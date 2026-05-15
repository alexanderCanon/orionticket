package com.orionticket.acesscontrol.infrastructure.adapters.out.messaging.event;

import java.time.Instant;
import java.util.UUID;

public record ConflictDetectedEvent(
        String eventType,
        UUID eventId,
        Instant occurredAt,
        ConflictDetectedPayload payload
) {
    public record ConflictDetectedPayload(
            UUID validationId,
            UUID ticketId,
            String validatorDeviceId,
            UUID conflictWithValidationId,
            UUID eventEntityId,
            UUID dateId
    ) {}
}