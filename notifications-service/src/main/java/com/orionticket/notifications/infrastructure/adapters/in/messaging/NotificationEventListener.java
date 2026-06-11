package com.orionticket.notifications.infrastructure.adapters.in.messaging;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orionticket.notifications.application.port.in.RegisterNotificationUseCase;
import com.orionticket.notifications.domain.model.Notification;
import com.orionticket.notifications.domain.model.NotificationChannel;
import com.orionticket.notifications.domain.model.NotificationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);
    private final RegisterNotificationUseCase registerNotificationUseCase;
    private final com.orionticket.notifications.application.port.in.DispatchNotificationUseCase dispatchNotificationUseCase;
    private final ObjectMapper objectMapper; // For parsing event payload

    public NotificationEventListener(
            RegisterNotificationUseCase registerNotificationUseCase,
            com.orionticket.notifications.application.port.in.DispatchNotificationUseCase dispatchNotificationUseCase,
            ObjectMapper objectMapper
    ) {
        this.registerNotificationUseCase = registerNotificationUseCase;
        this.dispatchNotificationUseCase = dispatchNotificationUseCase;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "notification-events")
    public void handleNotificationEvent(String message) {
        log.info("Received notification event: {}", message);
        try {
            // Assuming the message is a JSON string representing the event
            // and contains enough information to construct a Notification.
            // In a real scenario, you'd have specific event DTOs.
            Map<String, Object> eventData = objectMapper.readValue(message, new TypeReference<Map<String, Object>>() {});

            // Extract necessary fields from eventData to construct a Notification
            // This is a simplified example. Real implementation would be more robust.
            UUID notificationId = UUID.randomUUID();
            UUID recipientId = UUID.fromString((String) eventData.get("recipientId"));
            NotificationChannel channel = NotificationChannel.valueOf((String) eventData.get("channel"));
            String templateId = (String) eventData.get("templateId");
            String payload = objectMapper.writeValueAsString(eventData.get("payload")); // Convert payload map back to JSON string
            String triggeredBy = (String) eventData.get("eventType"); // Assuming eventType is the trigger

            Notification newNotification = new Notification(
                    notificationId,
                    recipientId,
                    channel,
                    templateId,
                    payload,
                    NotificationStatus.PENDING, // Always PENDING initially
                    0, // Initial retry count
                    triggeredBy,
                    Instant.now()
            );

            registerNotificationUseCase.register(newNotification);
            log.info("Notification registered successfully for event: {}. Initiating dispatch.", triggeredBy);

            try {
                dispatchNotificationUseCase.dispatchNotification(
                        new com.orionticket.notifications.application.port.in.command.DispatchNotificationCommand(notificationId)
                );
                log.info("Notification dispatch completed successfully for notification: {}", notificationId);
            } catch (Exception dispatchEx) {
                log.error("Failed to automatically dispatch notification: {}", notificationId, dispatchEx);
            }

        } catch (Exception e) {
            log.error("Error processing notification event: {}", message, e);
            // Depending on the error, you might want to send to a DLQ or log for manual inspection.
        }
    }
}
