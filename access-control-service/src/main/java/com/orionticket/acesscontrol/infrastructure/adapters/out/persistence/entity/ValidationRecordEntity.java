package com.orionticket.acesscontrol.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "validation_record")
public class ValidationRecordEntity {

    @Id
    @Column(name = "validation_id", nullable = false)
    private UUID validationId;

    @Column(name = "ticket_id", nullable = false)
    private UUID ticketId;

    @Column(name = "validator_device_id", nullable = false, length = 100)
    private String validatorDeviceId;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "date_id", nullable = false)
    private UUID dateId;

    @Column(name = "attempted_at", nullable = false)
    private Instant attemptedAt;

    @Column(name = "result", nullable = false, length = 20)
    private String result;

    @Column(name = "failure_reason", length = 50)
    private String failureReason;

    @Column(name = "is_offline", nullable = false)
    private boolean isOffline;

    @Column(name = "synced_at")
    private Instant syncedAt;

    @Column(name = "conflict_detected", nullable = false)
    private boolean conflictDetected;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public ValidationRecordEntity() {}

    public UUID getValidationId() {
        return validationId;
    }

    public void setValidationId(UUID validationId) {
        this.validationId = validationId;
    }

    public UUID getTicketId() {
        return ticketId;
    }

    public void setTicketId(UUID ticketId) {
        this.ticketId = ticketId;
    }

    public String getValidatorDeviceId() {
        return validatorDeviceId;
    }

    public void setValidatorDeviceId(String validatorDeviceId) {
        this.validatorDeviceId = validatorDeviceId;
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public UUID getDateId() {
        return dateId;
    }

    public void setDateId(UUID dateId) {
        this.dateId = dateId;
    }

    public Instant getAttemptedAt() {
        return attemptedAt;
    }

    public void setAttemptedAt(Instant attemptedAt) {
        this.attemptedAt = attemptedAt;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public boolean isOffline() {
        return isOffline;
    }

    public void setOffline(boolean offline) {
        isOffline = offline;
    }

    public Instant getSyncedAt() {
        return syncedAt;
    }

    public void setSyncedAt(Instant syncedAt) {
        this.syncedAt = syncedAt;
    }

    public boolean isConflictDetected() {
        return conflictDetected;
    }

    public void setConflictDetected(boolean conflictDetected) {
        this.conflictDetected = conflictDetected;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}