package com.orionticket.notifications.application.service;

import com.orionticket.notifications.application.port.in.RegisterNotificationUseCase;
import com.orionticket.notifications.domain.model.Notification;
import com.orionticket.notifications.domain.port.out.NotificationRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterNotificationService implements RegisterNotificationUseCase {

    private final NotificationRepositoryPort notificationRepository;

    public RegisterNotificationService(NotificationRepositoryPort notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional
    public Notification register(Notification notification) {
        return notificationRepository.save(notification);
    }
}
