package com.orionticket.notifications.application.service;

import com.orionticket.notifications.application.port.in.RetryNotificationUseCase;
import com.orionticket.notifications.domain.model.Notification;
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
            NotificationSenderPort notificationSender
    ) {
        this.notificationRepository = notificationRepository;
        this.notificationSender = notificationSender;
    }

    @Override
    @Transactional
    public List<Notification> retryFailedNotifications() {
        List<Notification> notificationsToRetry = new ArrayList<>();
        notificationsToRetry.addAll(notificationRepository.findByStatus(NotificationStatus.FAILED));
        notificationsToRetry.addAll(notificationRepository.findByStatus(NotificationStatus.PENDING));

        List<Notification> updatedNotifications = new ArrayList<>();

        for (Notification notification : notificationsToRetry) {
            // Attempt to send the notification
            boolean sentSuccessfully = notificationSender.send(notification);

            NotificationStatus newStatus = sentSuccessfully ? NotificationStatus.DELIVERED : NotificationStatus.FAILED;
            int newRetryCount = notification.retryCount() + 1;

            // Create a new Notification instance with updated status and retry count
            Notification retriedNotification = new Notification(
                    notification.notificationId(),
                    notification.recipientId(),
                    notification.channel(),
                    notification.templateId(),
                    notification.payload(),
                    newStatus,
                    newRetryCount,
                    notification.triggeredBy(),
                    notification.createdAt()
            );

            updatedNotifications.add(notificationRepository.save(retriedNotification));
        }
        return updatedNotifications;
    }
}
