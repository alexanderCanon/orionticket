package com.orionticket.notifications.application.port.in;

import com.orionticket.notifications.domain.model.Notification;

import java.util.List;
import java.util.UUID;

public interface RetryNotificationUseCase {
    Notification retryNotification(UUID notificationId);

    List<Notification> retryFailedNotifications();
}
