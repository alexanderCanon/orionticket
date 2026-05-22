package com.orionticket.notifications.domain.exception;

import com.orionticket.notifications.domain.model.NotificationStatus;

import java.util.UUID;

public class NotificationRetryNotAllowedException extends RuntimeException {

    public NotificationRetryNotAllowedException(UUID notificationId, NotificationStatus status) {
        super("Notification " + notificationId + " cannot be retried from status " + status);
    }
}
