package com.orionticket.notifications.domain.exception;

import java.util.UUID;

public class NotificationNotFoundException extends RuntimeException {
    public NotificationNotFoundException(UUID notificationId) {
        super("Notification with ID " + notificationId + " not found");
    }
}
