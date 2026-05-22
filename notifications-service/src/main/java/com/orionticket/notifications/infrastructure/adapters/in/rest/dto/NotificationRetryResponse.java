package com.orionticket.notifications.infrastructure.adapters.in.rest.dto;

import java.util.UUID;

public record NotificationRetryResponse(
        UUID notificationId,
        UUID recipientId,
        String channel,
        String status,
        int retryCount
) {
}
