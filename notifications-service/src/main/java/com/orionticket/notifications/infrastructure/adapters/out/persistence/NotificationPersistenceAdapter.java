package com.orionticket.notifications.infrastructure.adapters.out.persistence;

import com.orionticket.notifications.domain.model.Notification;
import com.orionticket.notifications.domain.model.NotificationStatus;
import com.orionticket.notifications.domain.port.out.NotificationRepositoryPort;
import com.orionticket.notifications.infrastructure.adapters.out.persistence.mapper.NotificationPersistenceMapper;
import com.orionticket.notifications.infrastructure.adapters.out.persistence.repository.JpaNotificationRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
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

    @Override
    public List<Notification> findByStatus(NotificationStatus status) {
        return jpaNotificationRepository.findByStatus(status.name()).stream()
                .map(notificationPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Notification> findById(UUID notificationId) {
        return jpaNotificationRepository.findById(notificationId)
                .map(notificationPersistenceMapper::toDomain);
    }
}
