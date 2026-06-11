package com.orionticket.payments.infrastructure.adapters.out.gateway;

import com.orionticket.payments.application.port.out.PaymentGatewayPort;
import com.orionticket.payments.domain.exception.PaymentGatewayException;
import com.stripe.StripeClient;
import com.stripe.exception.IdempotencyException;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Stripe payment gateway adapter.
 *
 * PCI note (BR-PA-08): gatewayToken is a Stripe PaymentMethod ID (pm_xxxx),
 * created client-side with Stripe.js. Raw card data never passes through this
 * service.
 *
 * Amount convention: Stripe requires amounts in the smallest currency unit.
 * For GTQ (Guatemalan Quetzal), 1 GTQ = 100 centavos → multiply BigDecimal by
 * 100.
 *
 * Idempotency: Stripe supports idempotency keys on the API request header
 * (BR-PA-09).
 * We pass our domain idempotencyKey directly to Stripe.
 */
@Component
public class StripePaymentGatewayAdapter implements PaymentGatewayPort {

    private static final Logger log = LoggerFactory.getLogger(StripePaymentGatewayAdapter.class);
    private static final String CURRENCY = "gtq";

    private final StripeClient stripeClient;

    public StripePaymentGatewayAdapter(StripeClient stripeClient) {
        this.stripeClient = stripeClient;
    }

    /**
     * Creates a Stripe PaymentIntent and confirms it immediately.
     * The final AUTHORIZED/FAILED result arrives via Stripe webhook.
     *
     * @param request domain gateway request
     * @return GatewayResponse with the PaymentIntent ID as gatewayReference
     * @throws PaymentGatewayException on Stripe API errors
     */
    @Override
    @SuppressWarnings("deprecation")
    public GatewayResponse process(GatewayRequest request) {
        log.info("Submitting payment to Stripe — idempotencyKey={} amount={} currency={}",
                request.idempotencyKey(), request.amount(), request.currency());

        long amountInCents = toSmallestUnit(request.amount());

        // MVP local simulation: bypass Stripe API if using test token or placeholder key
        if (request.gatewayToken().startsWith("tok_") || stripeClient == null) {
            log.info("MVP SIMULATION: Auto-approving payment with test token");
            return new GatewayResponse(true, "pi_simulated_" + request.idempotencyKey(), null);
        }

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency(CURRENCY)
                .setPaymentMethod(request.gatewayToken())
                .setConfirm(true) // confirm immediately
                .setReturnUrl("https://orionticket.com") // required for 3DS redirects
                .putMetadata("paymentId", request.paymentId().toString())
                .putMetadata("orderId", request.orderId().toString())
                .build();

        try {
            com.stripe.net.RequestOptions options = com.stripe.net.RequestOptions.builder()
                    .setIdempotencyKey(request.idempotencyKey())
                    .build();

            PaymentIntent intent = stripeClient.paymentIntents().create(params, options);

            log.info("Stripe PaymentIntent created — id={} status={}", intent.getId(), intent.getStatus());
            return new GatewayResponse(true, intent.getId(), null);

        } catch (IdempotencyException e) {
            // Idempotent replay: Stripe returned cached response from a previous request
            // with same key
            log.info("Stripe idempotency hit — returning cached result for key={}", request.idempotencyKey());
            return new GatewayResponse(true, e.getStripeError().getPaymentIntent().getId(), null);
        } catch (StripeException e) {
            log.error("Stripe API error — code={} message={}", e.getCode(), e.getMessage());
            return new GatewayResponse(false, null, e.getMessage());
        }
    }

    /**
     * Converts a BigDecimal amount to the smallest currency unit (centavos for
     * GTQ).
     */
    private long toSmallestUnit(BigDecimal amount) {
        return amount.multiply(new BigDecimal("100")).longValue();
    }

}
