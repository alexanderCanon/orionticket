package com.orionticket.notifications.application.port.in;

import com.orionticket.notifications.application.port.in.command.DispatchNotificationCommand;
import com.orionticket.notifications.domain.model.Notification;

public interface DispatchNotificationUseCase {
    Notification dispatchNotification(DispatchNotificationCommand command);
}
