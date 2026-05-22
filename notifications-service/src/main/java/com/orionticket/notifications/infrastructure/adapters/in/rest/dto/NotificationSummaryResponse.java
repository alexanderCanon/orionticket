package com.orionticket.notifications.infrastructure.adapters.in.rest.dto;

import java.time.Instant;
import java.util.UUID;

public record NotificationSummaryResponse(
        UUID notificationId,
        UUID recipientId,
        String channel,
        String templateId,
        String status,
        int retryCount,
        String triggeredBy,
        Instant createdAt
) {
}
