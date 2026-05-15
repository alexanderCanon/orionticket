package com.orionticket.notifications.domain.port.out;

import com.orionticket.notifications.domain.model.Notification;

public interface NotificationSenderPort {
    boolean send(Notification notification);
}
