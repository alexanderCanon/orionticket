package com.orionticket.payments.domain.model;

import com.orionticket.payments.domain.exception.InvalidPaymentStateException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Payout {

    /** Maximum number of automatic retries allowed on failure (BR-PA-06). */
    public static final int MAX_RETRY_COUNT = 1;

    private UUID payoutId;
    private UUID organizerId;
    private UUID eventId;
    private UUID dateId;
    private BigDecimal grossAmount;
    private BigDecimal serviceFeeTotal;
    private BigDecimal netAmount;
    private PayoutStatus status;
    private int retryCount;
    private Instant triggeredAt;
    private Instant processedAt;

    public Payout() {
    }

    public Payout(UUID payoutId, UUID organizerId, UUID eventId, UUID dateId,
                  BigDecimal grossAmount, BigDecimal serviceFeeTotal, BigDecimal netAmount,
                  PayoutStatus status, int retryCount, Instant triggeredAt, Instant processedAt) {
        this.payoutId = payoutId;
        this.organizerId = organizerId;
        this.eventId = eventId;
        this.dateId = dateId;
        this.grossAmount = grossAmount;
        this.serviceFeeTotal = serviceFeeTotal;
        this.netAmount = netAmount;
        this.status = status;
        this.retryCount = retryCount;
        this.triggeredAt = triggeredAt;
        this.processedAt = processedAt;
    }

    // -------------------------------------------------------------------------
    // Domain behaviour
    // -------------------------------------------------------------------------

    /**
     * Factory method. Creates a new Payout in PENDING state triggered after a
     * Date passes (ADR-009). Validates financial invariants.
     *
     * @param organizerId    the Organizer to receive the settlement (BR-PA-05)
     * @param eventId        reference to the Event (cross-service ID)
     * @param dateId         the specific Date that has passed (ADR-009)
     * @param grossAmount    total revenue collected for this dateId
     * @param serviceFeeTotal total service fees retained by the platform (BR-PA-04)
     * @return a new Payout in PENDING status
     */
    public static Payout generate(UUID organizerId, UUID eventId, UUID dateId,
                                  BigDecimal grossAmount, BigDecimal serviceFeeTotal) {
        if (organizerId == null) throw new IllegalArgumentException("organizerId is required");
        if (eventId == null) throw new IllegalArgumentException("eventId is required");
        if (dateId == null) throw new IllegalArgumentException("dateId is required");
        if (grossAmount == null || grossAmount.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("grossAmount must be non-negative");
        if (serviceFeeTotal == null || serviceFeeTotal.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("serviceFeeTotal must be non-negative");
        if (serviceFeeTotal.compareTo(grossAmount) > 0)
            throw new IllegalArgumentException("serviceFeeTotal cannot exceed grossAmount");

        BigDecimal netAmount = grossAmount.subtract(serviceFeeTotal);

        return new Payout(
                UUID.randomUUID(),
                organizerId,
                eventId,
                dateId,
                grossAmount,
                serviceFeeTotal,
                netAmount,
                PayoutStatus.PENDING,
                0,
                Instant.now(),
                null
        );
    }

    /**
     * Transitions the Payout from PENDING to PROCESSED after the settlement
     * transfer to the Organizer is confirmed.
     *
     * @param processedAt timestamp when the transfer was confirmed
     * @throws InvalidPaymentStateException if the Payout is not in PENDING status
     */
    public void markProcessed(Instant processedAt) {
        if (this.status != PayoutStatus.PENDING) {
            throw new InvalidPaymentStateException(
                    "Payout can only be processed from PENDING status. Current: " + this.status);
        }
        this.status = PayoutStatus.PROCESSED;
        this.processedAt = processedAt;
    }

    /**
     * Transitions the Payout from PENDING to FAILED.
     * A failed Payout may be retried if retryCount < MAX_RETRY_COUNT (BR-PA-06).
     *
     * @throws InvalidPaymentStateException if the Payout is not in PENDING status
     */
    public void markFailed() {
        if (this.status != PayoutStatus.PENDING) {
            throw new InvalidPaymentStateException(
                    "Payout can only be failed from PENDING status. Current: " + this.status);
        }
        this.status = PayoutStatus.FAILED;
    }

    /**
     * Resets a FAILED Payout back to PENDING for automatic retry.
     * Enforces the maximum of 1 automatic retry (BR-PA-06).
     *
     * @throws InvalidPaymentStateException if the Payout is not FAILED or has exhausted retries
     */
    public void retry() {
        if (this.status != PayoutStatus.FAILED) {
            throw new InvalidPaymentStateException(
                    "Only FAILED payouts can be retried. Current: " + this.status);
        }
        if (this.retryCount >= MAX_RETRY_COUNT) {
            throw new InvalidPaymentStateException(
                    "Payout has exhausted the maximum automatic retries (" + MAX_RETRY_COUNT + ")");
        }
        this.retryCount++;
        this.status = PayoutStatus.PENDING;
    }

    // -------------------------------------------------------------------------
    // Getters and setters (infrastructure mapping requires setters)
    // -------------------------------------------------------------------------

    public UUID getPayoutId() { return payoutId; }
    public void setPayoutId(UUID payoutId) { this.payoutId = payoutId; }

    public UUID getOrganizerId() { return organizerId; }
    public void setOrganizerId(UUID organizerId) { this.organizerId = organizerId; }

    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }

    public UUID getDateId() { return dateId; }
    public void setDateId(UUID dateId) { this.dateId = dateId; }

    public BigDecimal getGrossAmount() { return grossAmount; }
    public void setGrossAmount(BigDecimal grossAmount) { this.grossAmount = grossAmount; }

    public BigDecimal getServiceFeeTotal() { return serviceFeeTotal; }
    public void setServiceFeeTotal(BigDecimal serviceFeeTotal) { this.serviceFeeTotal = serviceFeeTotal; }

    public BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }

    public PayoutStatus getStatus() { return status; }
    public void setStatus(PayoutStatus status) { this.status = status; }

    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }

    public Instant getTriggeredAt() { return triggeredAt; }
    public void setTriggeredAt(Instant triggeredAt) { this.triggeredAt = triggeredAt; }

    public Instant getProcessedAt() { return processedAt; }
    public void setProcessedAt(Instant processedAt) { this.processedAt = processedAt; }

    // -------------------------------------------------------------------------
    // Enum
    // -------------------------------------------------------------------------

    public enum PayoutStatus {
        PENDING,
        PROCESSED,
        FAILED
    }
}