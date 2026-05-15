package com.orionticket.notifications.infrastructure.adapters.out.sender;

import com.orionticket.notifications.domain.model.Notification;
import com.orionticket.notifications.domain.port.out.NotificationSenderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EmailSenderAdapter implements NotificationSenderPort {

    private static final Logger log = LoggerFactory.getLogger(EmailSenderAdapter.class);

    @Override
    public boolean send(Notification notification) {
        // Simulate sending an email
        log.info("Attempting to send email notification: {}", notification);

        // In a real application, this would involve calling an external email service API.
        // For now, we'll simulate success.
        boolean success = Math.random() > 0.1; // 90% success rate for simulation

        if (success) {
            log.info("Successfully sent email notification: {}", notification.notificationId());
        } else {
            log.warn("Failed to send email notification: {}", notification.notificationId());
        }
        return success;
    }
}
