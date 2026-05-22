package com.orionticket.notifications.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Notification {

    private final UUID notificationId;
    private final UUID recipientId;
    private final NotificationChannel channel;
    private final String templateId;
    private final String payload;
    private final NotificationStatus status;
    private final int retryCount;
    private final String triggeredBy;
    private final Instant createdAt;
    private final String providerMessageId;
    private final String failureReason;

    public Notification(
            UUID notificationId,
            UUID recipientId,
            NotificationChannel channel,
            String templateId,
            String payload,
            NotificationStatus status,
            int retryCount,
            String triggeredBy,
            Instant createdAt,
            String providerMessageId,
            String failureReason
    ) {
        this.notificationId = Objects.requireNonNull(notificationId, "notificationId is required");
        this.recipientId = Objects.requireNonNull(recipientId, "recipientId is required");
        this.channel = Objects.requireNonNull(channel, "channel is required");
        this.templateId = requireText(templateId, "templateId is required");
        this.payload = Objects.requireNonNull(payload, "payload is required");
        this.status = Objects.requireNonNull(status, "status is required");
        this.triggeredBy = requireText(triggeredBy, "triggeredBy is required");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.providerMessageId = providerMessageId;
        this.failureReason = failureReason;

        if (retryCount < 0) {
            throw new IllegalArgumentException("retryCount cannot be negative");
        }
        this.retryCount = retryCount;
    }

    public Notification(
            UUID notificationId,
            UUID recipientId,
            NotificationChannel channel,
            String templateId,
            String payload,
            NotificationStatus status,
            int retryCount,
            String triggeredBy,
            Instant createdAt
    ) {
        this(notificationId, recipientId, channel, templateId, payload, status, retryCount, triggeredBy, createdAt, null, null);
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    public UUID notificationId() {
        return notificationId;
    }

    public UUID recipientId() {
        return recipientId;
    }

    public NotificationChannel channel() {
        return channel;
    }

    public String templateId() {
        return templateId;
    }

    public String payload() {
        return payload;
    }

    public NotificationStatus status() {
        return status;
    }

    public int retryCount() {
        return retryCount;
    }

    public String triggeredBy() {
        return triggeredBy;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public String providerMessageId() {
        return providerMessageId;
    }

    public String failureReason() {
        return failureReason;
    }
}
