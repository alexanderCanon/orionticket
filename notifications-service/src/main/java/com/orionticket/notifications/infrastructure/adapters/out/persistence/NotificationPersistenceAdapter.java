package com.orionticket.notifications.infrastructure.adapters.out.persistence;

import com.orionticket.notifications.domain.model.Notification;
import com.orionticket.notifications.domain.model.NotificationChannel;
import com.orionticket.notifications.domain.model.NotificationStatus;
import com.orionticket.notifications.domain.port.out.NotificationRepositoryPort;
import com.orionticket.notifications.infrastructure.adapters.out.persistence.mapper.NotificationPersistenceMapper;
import com.orionticket.notifications.infrastructure.adapters.out.persistence.repository.JpaNotificationRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
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

    @Override
    public List<Notification> findByFilters(
            UUID recipientId,
            NotificationStatus status,
            NotificationChannel channel,
            String triggeredBy,
            int page,
            int size
    ) {
        return jpaNotificationRepository.findAll(
                        filters(recipientId, status, channel, triggeredBy),
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
                )
                .stream()
                .map(notificationPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public long countByFilters(
            UUID recipientId,
            NotificationStatus status,
            NotificationChannel channel,
            String triggeredBy
    ) {
        return jpaNotificationRepository.count(filters(recipientId, status, channel, triggeredBy));
    }

    private Specification<com.orionticket.notifications.infrastructure.adapters.out.persistence.entity.NotificationEntity> filters(
            UUID recipientId,
            NotificationStatus status,
            NotificationChannel channel,
            String triggeredBy
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (recipientId != null) {
                predicates.add(criteriaBuilder.equal(root.get("recipientId"), recipientId));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status.name()));
            }
            if (channel != null) {
                predicates.add(criteriaBuilder.equal(root.get("channel"), channel.name()));
            }
            if (triggeredBy != null) {
                predicates.add(criteriaBuilder.equal(root.get("triggeredBy"), triggeredBy));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
