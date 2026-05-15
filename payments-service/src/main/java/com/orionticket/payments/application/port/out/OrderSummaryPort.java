package com.orionticket.payments.application.port.out;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Output port for fetching Order data from the Orders service.
 * Used by InitiatePaymentService to resolve amount, serviceFee and currency
 * without coupling the domain to HTTP or event projection details.
 */
public interface OrderSummaryPort {

    /**
     * Retrieve a summary of Order financial data needed to initiate a Payment.
     *
     * @param orderId the Order to look up
     * @return OrderSummary with financial data
     * @throws com.orionticket.payments.domain.exception.PaymentNotFoundException
     *         if the Order does not exist in the Orders service
     */
    OrderSummary findByOrderId(UUID orderId);

    /**
     * Snapshot of the Order fields Payments needs.
     * Only financial and status fields — no line items or seat data.
     */
    record OrderSummary(
            UUID orderId,
            UUID buyerId,
            BigDecimal total,        // amount the Buyer pays (subtotal + serviceFee - promotionDiscount)
            BigDecimal serviceFee,   // platform fee included in total (BR-PA-04)
            String currency,
            String status            // CREATED | PAYMENT_INITIATED | CONFIRMED | EXPIRED | FAILED
    ) {}
}
