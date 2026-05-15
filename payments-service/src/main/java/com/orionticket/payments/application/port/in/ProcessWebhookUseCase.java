package com.orionticket.payments.application.port.in;

import java.util.UUID;

/**
 * Use case for processing an incoming payment gateway webhook.
 * The webhook carries the final authorization result for a previously initiated Payment.
 * Related use cases: UC-PA-01, UC-PA-02.
 */
public interface ProcessWebhookUseCase {

    /**
     * Process a webhook notification from the payment gateway.
     * Transitions the Payment to AUTHORIZED or FAILED and publishes the corresponding
     * domain event. On failure, the caller (Orders service via event) must release
     * the Reservation and notify the Buyer (BR-PA-10).
     *
     * @param paymentId        the internal Payment UUID this webhook refers to
     * @param gatewayReference the external gateway transaction ID
     * @param result           "AUTHORIZED" or "FAILED"
     * @param failureReason    human-readable reason; null when result is AUTHORIZED
     */
    void processWebhook(UUID paymentId, String gatewayReference, String result, String failureReason);
}