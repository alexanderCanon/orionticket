package com.orionticket.notifications.application.service;

import com.orionticket.notifications.application.port.in.NotificationQueryUseCase;
import com.orionticket.notifications.application.port.in.model.NotificationPage;
import com.orionticket.notifications.domain.model.Notification;
import com.orionticket.notifications.domain.model.NotificationChannel;
import com.orionticket.notifications.domain.model.NotificationStatus;
import com.orionticket.notifications.domain.port.out.NotificationRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;

@Service
public class NotificationQueryService implements NotificationQueryUseCase {

    private final NotificationRepositoryPort notificationRepository;

    public NotificationQueryService(NotificationRepositoryPort notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationPage findNotifications(
            UUID recipientId,
            NotificationStatus status,
            NotificationChannel channel,
            String triggeredBy,
            int page,
            int size
    ) {
        String normalizedTriggeredBy = StringUtils.hasText(triggeredBy) ? triggeredBy : null;
        long totalElements = notificationRepository.countByFilters(recipientId, status, channel, normalizedTriggeredBy);
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);

        return new NotificationPage(
                notificationRepository.findByFilters(recipientId, status, channel, normalizedTriggeredBy, page, size),
                page,
                totalPages
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Notification> findNotification(UUID notificationId) {
        return notificationRepository.findById(notificationId);
    }
}
