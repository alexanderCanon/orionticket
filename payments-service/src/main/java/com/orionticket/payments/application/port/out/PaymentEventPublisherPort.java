package com.orionticket.payments.application.port.out;

import java.math.BigDecimal;

/**
 * Output port for publishing domain events to the message broker.
 * All events must match the schemas defined in docs/phases/phase-3/event-schemas.md.
 */
public interface PaymentEventPublisherPort {

    void publishPaymentInitiated(PaymentEvent event);

    void publishPaymentAuthorized(PaymentEvent event);

    void publishPaymentFailed(PaymentFailedEvent event);

    void publishPayoutGenerated(PayoutEvent event);

    void publishPayoutProcessed(PayoutEvent event);

    // -------------------------------------------------------------------------
    // Event payload records — fields match event-schemas.md exactly
    // -------------------------------------------------------------------------

    /**
     * Payload for PaymentInitiated and PaymentAuthorized events.
     * Consumers: Orders (AUTHORIZED), Ticket Issuance (AUTHORIZED).
     */
    record PaymentEvent(
            String paymentId,
            String orderId,
            String buyerId,
            BigDecimal amount,
            BigDecimal serviceFee,
            String currency,
            String method,
            String gatewayReference,   // null for PaymentInitiated, set for PaymentAuthorized
            String idempotencyKey,
            String status
    ) {}

    /**
     * Payload for PaymentFailed event.
     * Consumers: Orders, Notifications, Seating/Inventory (to release Reservation — BR-PA-10).
     */
    record PaymentFailedEvent(
            String paymentId,
            String orderId,
            String buyerId,
            BigDecimal amount,
            String currency,
            String method,
            String failureReason,
            String status
    ) {}

    /**
     * Payload for PayoutGenerated and PayoutProcessed events.
     */
    record PayoutEvent(
            String payoutId,
            String organizerId,
            String eventId,
            String dateId,
            BigDecimal grossAmount,
            BigDecimal serviceFeeTotal,
            BigDecimal netAmount,
            String status
    ) {}
}