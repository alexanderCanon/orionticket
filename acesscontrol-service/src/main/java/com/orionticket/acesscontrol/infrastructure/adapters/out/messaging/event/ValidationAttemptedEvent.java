package com.orionticket.acesscontrol.infrastructure.adapters.out.messaging.event;

import java.time.Instant;
import java.util.UUID;

public record ValidationAttemptedEvent(
        String eventType,
        UUID eventId,
        Instant occurredAt,
        ValidationAttemptedPayload payload
) {
    public record ValidationAttemptedPayload(
            UUID validationId,
            UUID ticketId,
            String validatorDeviceId,
            UUID eventEntityId,
            UUID dateId,
            Instant attemptedAt,
            boolean isOffline
    ) {}
}