package com.orionticket.notifications.domain.port.out;

import com.orionticket.notifications.domain.model.Notification;
import com.orionticket.notifications.domain.model.NotificationStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepositoryPort {

    Notification save(Notification notification);

    boolean existsById(UUID notificationId);

    List<Notification> findByStatus(NotificationStatus status);

    Optional<Notification> findById(UUID notificationId);
}
