package com.orionticket.notifications.application.service;

import com.orionticket.notifications.application.port.in.DispatchNotificationUseCase;
import com.orionticket.notifications.application.port.in.command.DispatchNotificationCommand;
import com.orionticket.notifications.domain.exception.NotificationNotFoundException;
import com.orionticket.notifications.domain.model.Notification;
import com.orionticket.notifications.domain.model.NotificationStatus;
import com.orionticket.notifications.domain.port.out.NotificationRepositoryPort;
import com.orionticket.notifications.domain.port.out.NotificationSenderPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DispatchNotificationService implements DispatchNotificationUseCase {

    private final NotificationRepositoryPort notificationRepository;
    private final NotificationSenderPort notificationSender;

    public DispatchNotificationService(
            NotificationRepositoryPort notificationRepository,
            NotificationSenderPort notificationSender) {
        this.notificationRepository = notificationRepository;
        this.notificationSender = notificationSender;
    }

    @Override
    @Transactional
    public Notification dispatchNotification(DispatchNotificationCommand command) {
        Notification notificationToDispatch = notificationRepository.findById(command.notificationId())
                .orElseThrow(() -> new NotificationNotFoundException(command.notificationId()));

        // Attempt to send the notification
        com.orionticket.notifications.domain.model.NotificationSendResult result = notificationSender.send(notificationToDispatch);

        NotificationStatus newStatus = result.success() ? NotificationStatus.DELIVERED : NotificationStatus.FAILED;

        // Create a new Notification instance with updated status, providerMessageId, and failureReason
        Notification dispatchedNotification = new Notification(
                notificationToDispatch.notificationId(),
                notificationToDispatch.recipientId(),
                notificationToDispatch.channel(),
                notificationToDispatch.templateId(),
                notificationToDispatch.payload(),
                newStatus,
                notificationToDispatch.retryCount(),
                notificationToDispatch.triggeredBy(),
                notificationToDispatch.createdAt(),
                result.providerMessageId(),
                result.failureReason());

        return notificationRepository.save(dispatchedNotification);
    }
}
