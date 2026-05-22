package com.orionticket.notifications.domain.model;

public record NotificationSendResult(
    boolean success,
    String providerMessageId,
    String failureReason
) {
    public static NotificationSendResult success(String providerMessageId) {
        return new NotificationSendResult(true, providerMessageId, null);
    }

    public static NotificationSendResult failure(String failureReason) {
        return new NotificationSendResult(false, null, failureReason);
    }
}
