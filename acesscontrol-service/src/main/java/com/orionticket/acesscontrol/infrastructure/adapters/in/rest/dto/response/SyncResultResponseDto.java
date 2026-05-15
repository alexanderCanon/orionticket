package com.orionticket.acesscontrol.infrastructure.adapters.in.rest.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.UUID;

public record SyncResultResponseDto(
        UUID validationId,
        UUID ticketId,
        String result,
        String failureReason,
        @JsonProperty("conflictDetected") boolean conflictDetected,
        Instant syncedAt
) {}