package com.orionticket.payments.domain.model;

import com.orionticket.payments.domain.exception.InvalidPaymentStateException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Payment {

    private UUID paymentId;
    private UUID orderId;
    private UUID buyerId;
    private BigDecimal amount;
    private BigDecimal serviceFee;
    private String currency;
    private PaymentMethod method;
    private PaymentStatus status;
    private String gatewayReference;
    private String idempotencyKey;
    private Instant createdAt;

    public Payment() {
    }

    public Payment(UUID paymentId, UUID orderId, UUID buyerId, BigDecimal amount,
                   BigDecimal serviceFee, String currency, PaymentMethod method,
                   PaymentStatus status, String gatewayReference, String idempotencyKey,
                   Instant createdAt) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.buyerId = buyerId;
        this.amount = amount;
        this.serviceFee = serviceFee;
        this.currency = currency;
        this.method = method;
        this.status = status;
        this.gatewayReference = gatewayReference;
        this.idempotencyKey = idempotencyKey;
        this.createdAt = createdAt;
    }

    // -------------------------------------------------------------------------
    // Domain behaviour
    // -------------------------------------------------------------------------

    /**
     * Factory method. Creates a new Payment in INITIATED state.
     * Validates all mandatory fields to prevent constructing an invalid aggregate.
     *
     * @param orderId        reference to the originating Order (BR-PA-01)
     * @param buyerId        reference to the paying Buyer
     * @param amount         total amount charged to the Buyer
     * @param serviceFee     platform service fee included in amount (BR-PA-04)
     * @param currency       ISO-4217 currency code; must be GTQ for v1 (BR-CC-10)
     * @param method         CARD or TRANSFER (BR-PA-02)
     * @param idempotencyKey unique key to prevent duplicate charges (BR-PA-09)
     * @return a new Payment in INITIATED status
     */
    public static Payment initiate(UUID orderId, UUID buyerId, BigDecimal amount,
                                   BigDecimal serviceFee, String currency,
                                   PaymentMethod method, String idempotencyKey) {
        if (orderId == null) throw new IllegalArgumentException("orderId is required");
        if (buyerId == null) throw new IllegalArgumentException("buyerId is required");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("amount must be positive");
        if (serviceFee == null || serviceFee.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("serviceFee must be non-negative");
        if (serviceFee.compareTo(amount) > 0)
            throw new IllegalArgumentException("serviceFee cannot exceed amount");
        if (currency == null || currency.isBlank())
            throw new IllegalArgumentException("currency is required");
        if (method == null) throw new IllegalArgumentException("method is required");
        if (idempotencyKey == null || idempotencyKey.isBlank())
            throw new IllegalArgumentException("idempotencyKey is required");

        return new Payment(
                UUID.randomUUID(),
                orderId,
                buyerId,
                amount,
                serviceFee,
                currency,
                method,
                PaymentStatus.INITIATED,
                null,           // gatewayReference — assigned by the gateway after processing
                idempotencyKey,
                Instant.now()
        );
    }

    /**
     * Transitions the Payment from INITIATED to AUTHORIZED after the payment
     * gateway confirms the transaction. Records the external gatewayReference.
     *
     * @param gatewayReference external transaction ID from the payment gateway
     * @throws InvalidPaymentStateException if the Payment is not in INITIATED status
     */
    public void authorize(String gatewayReference) {
        if (this.status != PaymentStatus.INITIATED) {
            throw new InvalidPaymentStateException(
                    "Payment can only be authorized from INITIATED status. Current: " + this.status);
        }
        if (gatewayReference == null || gatewayReference.isBlank()) {
            throw new IllegalArgumentException("gatewayReference is required to authorize a payment");
        }
        this.status = PaymentStatus.AUTHORIZED;
        this.gatewayReference = gatewayReference;
    }

    /**
     * Transitions the Payment from INITIATED to FAILED when the payment gateway
     * rejects or fails the transaction.
     * After this call, the caller must publish PaymentFailed so the Reservation
     * is released and the Buyer is notified (BR-PA-10).
     *
     * @throws InvalidPaymentStateException if the Payment is not in INITIATED status
     */
    public void fail() {
        if (this.status != PaymentStatus.INITIATED) {
            throw new InvalidPaymentStateException(
                    "Payment can only be failed from INITIATED status. Current: " + this.status);
        }
        this.status = PaymentStatus.FAILED;
    }

    // -------------------------------------------------------------------------
    // Getters and setters (infrastructure mapping requires setters)
    // -------------------------------------------------------------------------

    public UUID getPaymentId() { return paymentId; }
    public void setPaymentId(UUID paymentId) { this.paymentId = paymentId; }

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }

    public UUID getBuyerId() { return buyerId; }
    public void setBuyerId(UUID buyerId) { this.buyerId = buyerId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getServiceFee() { return serviceFee; }
    public void setServiceFee(BigDecimal serviceFee) { this.serviceFee = serviceFee; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public PaymentMethod getMethod() { return method; }
    public void setMethod(PaymentMethod method) { this.method = method; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public String getGatewayReference() { return gatewayReference; }
    public void setGatewayReference(String gatewayReference) { this.gatewayReference = gatewayReference; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    // -------------------------------------------------------------------------
    // Enums
    // -------------------------------------------------------------------------

    public enum PaymentMethod {
        CARD,
        TRANSFER
    }

    public enum PaymentStatus {
        INITIATED,
        AUTHORIZED,
        FAILED
    }
}