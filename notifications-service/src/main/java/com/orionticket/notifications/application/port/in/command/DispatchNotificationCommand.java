package com.orionticket.notifications.application.port.in.command;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DispatchNotificationCommand(
        @NotNull UUID notificationId
) {
}
