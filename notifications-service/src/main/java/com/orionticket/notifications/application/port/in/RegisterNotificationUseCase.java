package com.orionticket.notifications.application.port.in;

import com.orionticket.notifications.domain.model.Notification;

public interface RegisterNotificationUseCase {

    Notification register(Notification notification);
}
