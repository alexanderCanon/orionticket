package com.orionticket.notifications.application.service;

import com.orionticket.notifications.domain.exception.NotificationRetryNotAllowedException;
import com.orionticket.notifications.domain.model.Notification;
import com.orionticket.notifications.domain.model.NotificationChannel;
import com.orionticket.notifications.domain.model.NotificationSendResult;
import com.orionticket.notifications.domain.model.NotificationStatus;
import com.orionticket.notifications.domain.port.out.NotificationRepositoryPort;
import com.orionticket.notifications.domain.port.out.NotificationSenderPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RetryNotificationServiceTest {

    @Mock
    private NotificationRepositoryPort notificationRepository;

    @Mock
    private NotificationSenderPort notificationSender;

    @InjectMocks
    private RetryNotificationService retryNotificationService;

    @Test
    void retryNotificationSendsPendingNotificationAndPersistsResult() {
        Notification pending = notification(NotificationStatus.PENDING, 2);
        Notification delivered = notification(NotificationStatus.DELIVERED, 3);

        when(notificationRepository.findById(pending.notificationId())).thenReturn(Optional.of(pending));
        when(notificationSender.send(pending)).thenReturn(NotificationSendResult.success("re_123"));
        when(notificationRepository.save(org.mockito.ArgumentMatchers.any(Notification.class))).thenReturn(delivered);

        Notification result = retryNotificationService.retryNotification(pending.notificationId());

        assertThat(result.status()).isEqualTo(NotificationStatus.DELIVERED);
        verify(notificationSender).send(pending);
    }

    @Test
    void retryNotificationRejectsDeliveredNotification() {
        Notification delivered = notification(NotificationStatus.DELIVERED, 1);

        when(notificationRepository.findById(delivered.notificationId())).thenReturn(Optional.of(delivered));

        assertThatThrownBy(() -> retryNotificationService.retryNotification(delivered.notificationId()))
                .isInstanceOf(NotificationRetryNotAllowedException.class);
        verify(notificationSender, never()).send(delivered);
    }

    private Notification notification(NotificationStatus status, int retryCount) {
        return new Notification(
                UUID.randomUUID(),
                UUID.randomUUID(),
                NotificationChannel.EMAIL,
                "ticket-issued",
                "{\"ticketId\":\"ticket-1\"}",
                status,
                retryCount,
                "TICKET_ISSUED",
                Instant.parse("2026-05-22T00:00:00Z"),
                null,
                null
        );
    }
}
