package com.orionticket.acesscontrol.domain.model;

import java.time.Instant;
import java.util.UUID;

public record ValidationRecord(
        UUID validationId,
        UUID ticketId,
        String validatorDeviceId,
        UUID eventId,
        UUID dateId,
        Instant attemptedAt,
        ValidationResult result,
        FailureReason failureReason,
        boolean isOffline,
        Instant syncedAt,
        boolean conflictDetected
) {
    public static ValidationRecord create(
            UUID ticketId,
            String validatorDeviceId,
            UUID eventId,
            UUID dateId,
            ValidationResult result,
            FailureReason failureReason,
            boolean isOffline
    ) {
        return new ValidationRecord(
                UUID.randomUUID(),
                ticketId,
                validatorDeviceId,
                eventId,
                dateId,
                Instant.now(),
                result,
                failureReason,
                isOffline,
                null,
                false
        );
    }

    public ValidationRecord withConflictDetected(UUID conflictingValidationId) {
        return new ValidationRecord(
                this.validationId,
                this.ticketId,
                this.validatorDeviceId,
                this.eventId,
                this.dateId,
                this.attemptedAt,
                this.result,
                this.failureReason,
                this.isOffline,
                this.syncedAt,
                true
        );
    }

    public ValidationRecord withSyncedAt(Instant syncedAt) {
        return new ValidationRecord(
                this.validationId,
                this.ticketId,
                this.validatorDeviceId,
                this.eventId,
                this.dateId,
                this.attemptedAt,
                this.result,
                this.failureReason,
                this.isOffline,
                syncedAt,
                this.conflictDetected
        );
    }
}