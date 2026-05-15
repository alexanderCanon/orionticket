package com.orionticket.acesscontrol.infrastructure.adapters.out.messaging.event;

import java.time.Instant;
import java.util.UUID;

public record ValidationFailedEvent(
        String eventType,
        UUID eventId,
        Instant occurredAt,
        ValidationFailedPayload payload
) {
    public record ValidationFailedPayload(
            UUID validationId,
            UUID ticketId,
            String validatorDeviceId,
            UUID eventEntityId,
            UUID dateId,
            String result,
            String failureReason,
            boolean isOffline,
            Instant syncedAt
    ) {}
}