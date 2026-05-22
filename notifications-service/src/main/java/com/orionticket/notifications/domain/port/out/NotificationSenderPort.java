package com.orionticket.notifications.domain.port.out;

import com.orionticket.notifications.domain.model.Notification;
import com.orionticket.notifications.domain.model.NotificationSendResult;

public interface NotificationSenderPort {
    NotificationSendResult send(Notification notification);
}
