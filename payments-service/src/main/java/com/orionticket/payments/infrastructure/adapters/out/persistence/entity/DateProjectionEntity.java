package com.orionticket.payments.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Local projection of DateAdded events from the Event Management service.
 * Used by PayoutGenerationScheduler to detect dates that have passed (ADR-009).
 */
@Entity
@Table(name = "date_projections")
public class DateProjectionEntity {

    @Id
    @Column(name = "date_id")
    private UUID dateId;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Column(name = "payout_generated", nullable = false)
    private boolean payoutGenerated = false;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    public UUID getDateId() { return dateId; }
    public void setDateId(UUID dateId) { this.dateId = dateId; }

    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }

    public Instant getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(Instant scheduledAt) { this.scheduledAt = scheduledAt; }

    public boolean isPayoutGenerated() { return payoutGenerated; }
    public void setPayoutGenerated(boolean payoutGenerated) { this.payoutGenerated = payoutGenerated; }

    public Instant getReceivedAt() { return receivedAt; }
    public void setReceivedAt(Instant receivedAt) { this.receivedAt = receivedAt; }
}
