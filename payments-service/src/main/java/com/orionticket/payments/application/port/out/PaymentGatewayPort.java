package com.orionticket.payments.application.port.out;

import java.math.BigDecimal;

/**
 * Output port for interacting with the external payment gateway.
 * The gateway adapter (StripePaymentGatewayAdapter) implements this interface
 * and is responsible for any gateway-specific amount conversion (e.g. BigDecimal → centavos).
 */
public interface PaymentGatewayPort {

    /**
     * Submit a payment to the gateway for processing.
     * Returns a GatewayResponse with the submission reference;
     * the final result (AUTHORIZED | FAILED) arrives asynchronously via webhook.
     *
     * @param request payment submission details
     * @return GatewayResponse containing the gatewayReference for tracking
     * @throws com.orionticket.payments.domain.exception.PaymentGatewayException
     *         on connectivity or unexpected gateway errors
     */
    GatewayResponse process(GatewayRequest request);

    /**
     * Request representing the data sent to the payment gateway.
     *
     * @param idempotencyKey stable key to prevent duplicate charges (BR-PA-09)
     * @param paymentMethod  CARD or TRANSFER
     * @param gatewayToken   tokenized card or transfer reference — raw card data must not be sent (BR-PA-08)
     * @param amount         total amount in the domain's BigDecimal format; adapters convert to gateway units
     * @param currency       ISO-4217 currency code
     */
    record GatewayRequest(
            String idempotencyKey,
            String paymentMethod,
            String gatewayToken,
            BigDecimal amount,
            String currency
    ) {}

    /**
     * Response from the gateway after submission.
     *
     * @param success          true if the submission was accepted by the gateway
     * @param gatewayReference external transaction reference for tracking and webhook correlation
     * @param failureReason    human-readable failure description; null if success
     */
    record GatewayResponse(
            boolean success,
            String gatewayReference,
            String failureReason
    ) {}
}