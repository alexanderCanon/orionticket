package com.orionticket.notifications.infrastructure.adapters.in.rest.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

public record NotificationDetailResponse(
        UUID notificationId,
        UUID recipientId,
        String channel,
        String templateId,
        JsonNode payload,
        String status,
        int retryCount,
        String triggeredBy,
        Instant createdAt
) {
}
