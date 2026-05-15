package com.orionticket.acesscontrol.infrastructure.adapters.in.rest.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.UUID;

public record ValidationResponseDto(
        UUID validationId,
        UUID ticketId,
        String result,
        String failureReason,
        @JsonProperty("isOffline") boolean isOffline,
        Instant attemptedAt
) {}