package com.orionticket.notifications.infrastructure.adapters.in.rest.dto;

public record RetryFailedResponse(
        int processed,
        int delivered,
        int failed
) {
}
