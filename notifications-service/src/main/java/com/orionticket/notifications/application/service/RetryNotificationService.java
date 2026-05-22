package com.orionticket.notifications.application.service;

import com.orionticket.notifications.application.port.in.RetryNotificationUseCase;
import com.orionticket.notifications.domain.exception.NotificationNotFoundException;
import com.orionticket.notifications.domain.exception.NotificationRetryNotAllowedException;
import com.orionticket.notifications.domain.model.Notification;
import com.orionticket.notifications.domain.model.NotificationSendResult;
import com.orionticket.notifications.domain.model.NotificationStatus;
import com.orionticket.notifications.domain.port.out.NotificationRepositoryPort;
import com.orionticket.notifications.domain.port.out.NotificationSenderPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class RetryNotificationService implements RetryNotificationUseCase {

    private final NotificationRepositoryPort notificationRepository;
    private final NotificationSenderPort notificationSender;

    public RetryNotificationService(
            NotificationRepositoryPort notificationRepository,
            NotificationSenderPort notificationSender) {
        this.notificationRepository = notificationRepository;
        this.notificationSender = notificationSender;
    }

    @Override
    @Transactional
    public Notification retryNotification(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));

        if (notification.status() != NotificationStatus.FAILED && notification.status() != NotificationStatus.PENDING) {
            throw new NotificationRetryNotAllowedException(notification.notificationId(), notification.status());
        }

        return retry(notification);
    }

    @Override
    @Transactional
    public List<Notification> retryFailedNotifications() {
        List<Notification> notificationsToRetry = new ArrayList<>();
        notificationsToRetry.addAll(notificationRepository.findByStatus(NotificationStatus.FAILED));
        notificationsToRetry.addAll(notificationRepository.findByStatus(NotificationStatus.PENDING));

        List<Notification> updatedNotifications = new ArrayList<>();

        for (Notification notification : notificationsToRetry) {
            updatedNotifications.add(retry(notification));
        }
        return updatedNotifications;
    }

    private Notification retry(Notification notification) {
        NotificationSendResult result = notificationSender.send(notification);

        NotificationStatus newStatus = result.success() ? NotificationStatus.DELIVERED : NotificationStatus.FAILED;
        int newRetryCount = notification.retryCount() + 1;

        Notification retriedNotification = new Notification(
                notification.notificationId(),
                notification.recipientId(),
                notification.channel(),
                notification.templateId(),
                notification.payload(),
                newStatus,
                newRetryCount,
                notification.triggeredBy(),
                notification.createdAt(),
                result.providerMessageId(),
                result.failureReason());

        return notificationRepository.save(retriedNotification);
    }
}
