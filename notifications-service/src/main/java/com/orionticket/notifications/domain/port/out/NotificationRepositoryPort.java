package com.orionticket.notifications.domain.port.out;

import com.orionticket.notifications.domain.model.Notification;

import java.util.UUID;

public interface NotificationRepositoryPort {

    Notification save(Notification notification);

    boolean existsById(UUID notificationId);
}
