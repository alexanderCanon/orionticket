package com.orionticket.acesscontrol.infrastructure.adapters.out.messaging.event;

import java.time.Instant;
import java.util.UUID;

public record ValidatorSyncedEvent(
        String eventType,
        UUID eventId,
        Instant occurredAt,
        ValidatorSyncedPayload payload
) {
    public record ValidatorSyncedPayload(
            String validatorDeviceId,
            UUID eventEntityId,
            UUID dateId,
            int totalSynced,
            int conflictsDetected
    ) {}
}