package com.orionticket.notifications.infrastructure.adapters.out.persistence.repository;

import com.orionticket.notifications.infrastructure.adapters.out.persistence.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface JpaNotificationRepository extends JpaRepository<NotificationEntity, UUID>, JpaSpecificationExecutor<NotificationEntity> {
    List<NotificationEntity> findByStatus(String status);
}
