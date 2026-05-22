package com.orionticket.notifications.infrastructure.adapters.in.rest.dto;

import java.util.List;

public record NotificationListResponse(
        List<NotificationSummaryResponse> notifications,
        int page,
        int totalPages
) {
}
