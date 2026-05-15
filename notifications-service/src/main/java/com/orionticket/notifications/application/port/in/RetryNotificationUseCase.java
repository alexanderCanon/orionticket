package com.orionticket.notifications.application.port.in;

import com.orionticket.notifications.domain.model.Notification;

import java.util.List;

public interface RetryNotificationUseCase {
    List<Notification> retryFailedNotifications();
}
