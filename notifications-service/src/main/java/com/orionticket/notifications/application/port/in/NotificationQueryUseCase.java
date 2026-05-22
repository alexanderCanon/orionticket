package com.orionticket.notifications.application.port.in;

import com.orionticket.notifications.application.port.in.model.NotificationPage;
import com.orionticket.notifications.domain.model.Notification;
import com.orionticket.notifications.domain.model.NotificationChannel;
import com.orionticket.notifications.domain.model.NotificationStatus;

import java.util.Optional;
import java.util.UUID;

public interface NotificationQueryUseCase {

    NotificationPage findNotifications(
            UUID recipientId,
            NotificationStatus status,
            NotificationChannel channel,
            String triggeredBy,
            int page,
            int size
    );

    Optional<Notification> findNotification(UUID notificationId);
}
