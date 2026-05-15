package com.orionticket.ticketissuance.infrastructure.adapters.in.rest.dto;

import java.time.Instant;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String errorCode,
        String path,
        String traceId
) {
}
