package com.orionticket.acesscontrol.infrastructure.adapters.out.messaging.event;

import java.time.Instant;
import java.util.UUID;

public record ValidationSucceededEvent(
        String eventType,
        UUID eventId,
        Instant occurredAt,
        ValidationSucceededPayload payload
) {
    public record ValidationSucceededPayload(
            UUID validationId,
            UUID ticketId,
            String validatorDeviceId,
            UUID eventEntityId,
            UUID dateId,
            String result,
            boolean isOffline,
            Instant syncedAt
    ) {}
}