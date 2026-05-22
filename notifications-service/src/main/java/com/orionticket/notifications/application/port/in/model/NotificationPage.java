package com.orionticket.notifications.application.port.in.model;

import com.orionticket.notifications.domain.model.Notification;

import java.util.List;

public record NotificationPage(
        List<Notification> notifications,
        int page,
        int totalPages
) {
}
