package com.orionticket.notifications.infrastructure.adapters.out.persistence.repository;

import com.orionticket.notifications.infrastructure.adapters.out.persistence.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaNotificationRepository extends JpaRepository<NotificationEntity, UUID> {
}
