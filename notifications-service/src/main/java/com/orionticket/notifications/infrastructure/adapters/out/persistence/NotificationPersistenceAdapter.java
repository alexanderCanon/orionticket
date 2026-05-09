package com.orionticket.notifications.infrastructure.adapters.out.persistence;

import com.orionticket.notifications.domain.model.Notification;
import com.orionticket.notifications.domain.port.out.NotificationRepositoryPort;
import com.orionticket.notifications.infrastructure.adapters.out.persistence.mapper.NotificationPersistenceMapper;
import com.orionticket.notifications.infrastructure.adapters.out.persistence.repository.JpaNotificationRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class NotificationPersistenceAdapter implements NotificationRepositoryPort {

    private final JpaNotificationRepository jpaNotificationRepository;
    private final NotificationPersistenceMapper notificationPersistenceMapper;

    public NotificationPersistenceAdapter(
            JpaNotificationRepository jpaNotificationRepository,
            NotificationPersistenceMapper notificationPersistenceMapper
    ) {
        this.jpaNotificationRepository = jpaNotificationRepository;
        this.notificationPersistenceMapper = notificationPersistenceMapper;
    }

    @Override
    public Notification save(Notification notification) {
        return notificationPersistenceMapper.toDomain(
                jpaNotificationRepository.save(notificationPersistenceMapper.toEntity(notification))
        );
    }

    @Override
    public boolean existsById(UUID notificationId) {
        return jpaNotificationRepository.existsById(notificationId);
    }
}
