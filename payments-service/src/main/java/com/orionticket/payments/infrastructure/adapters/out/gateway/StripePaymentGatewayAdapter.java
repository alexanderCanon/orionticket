package com.orionticket.payments.infrastructure.adapters.out.gateway;

import com.orionticket.payments.application.port.out.PaymentGatewayPort;
import com.orionticket.payments.domain.exception.PaymentGatewayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Stripe payment gateway adapter.
 * Implements PaymentGatewayPort by translating domain requests into Stripe API calls.
 *
 * Important PCI note (BR-PA-08): gatewayToken is a Stripe PaymentMethod ID
 * (e.g. pm_xxxx) — raw card data is never sent through this service.
 * Stripe amount convention: multiply BigDecimal by 100 and send as long (centavos for GTQ).
 */
@Component
public class StripePaymentGatewayAdapter implements PaymentGatewayPort {

    private static final Logger log = LoggerFactory.getLogger(StripePaymentGatewayAdapter.class);

    @Override
    public GatewayResponse process(GatewayRequest request) {
        // TODO: integrate with Stripe SDK
        // Example flow:
        //   1. Build PaymentIntentCreateParams with request.amount() in centavos
        //   2. Call Stripe.PaymentIntent.create(params) with idempotency key header
        //   3. Map response to GatewayResponse
        log.warn("Stripe gateway process() not yet implemented — idempotencyKey={}", request.idempotencyKey());
        throw new PaymentGatewayException("Stripe gateway integration not implemented yet", null);
    }

}