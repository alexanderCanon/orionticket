package com.orionticket.notifications.infrastructure.adapters.out.persistence.mapper;

import com.orionticket.notifications.domain.model.Notification;
import com.orionticket.notifications.domain.model.NotificationChannel;
import com.orionticket.notifications.domain.model.NotificationStatus;
import com.orionticket.notifications.infrastructure.adapters.out.persistence.entity.NotificationEntity;
import org.springframework.stereotype.Component;

@Component
public class NotificationPersistenceMapper {

    public Notification toDomain(NotificationEntity entity) {
        return new Notification(
                entity.getNotificationId(),
                entity.getRecipientId(),
                NotificationChannel.valueOf(entity.getChannel()),
                entity.getTemplateId(),
                entity.getPayload(),
                NotificationStatus.valueOf(entity.getStatus()),
                entity.getRetryCount(),
                entity.getTriggeredBy(),
                entity.getCreatedAt(),
                entity.getProviderMessageId(),
                entity.getFailureReason()
        );
    }

    public NotificationEntity toEntity(Notification notification) {
        NotificationEntity entity = new NotificationEntity();
        entity.setNotificationId(notification.notificationId());
        entity.setRecipientId(notification.recipientId());
        entity.setChannel(notification.channel().name());
        entity.setTemplateId(notification.templateId());
        entity.setPayload(notification.payload());
        entity.setStatus(notification.status().name());
        entity.setRetryCount(notification.retryCount());
        entity.setTriggeredBy(notification.triggeredBy());
        entity.setCreatedAt(notification.createdAt());
        entity.setProviderMessageId(notification.providerMessageId());
        entity.setFailureReason(notification.failureReason());
        return entity;
    }
}
