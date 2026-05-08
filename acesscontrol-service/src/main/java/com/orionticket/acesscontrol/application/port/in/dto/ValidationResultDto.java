package com.orionticket.acesscontrol.application.port.in.dto;

import com.orionticket.acesscontrol.domain.model.FailureReason;
import com.orionticket.acesscontrol.domain.model.ValidationResult;
import java.time.Instant;
import java.util.UUID;

public record ValidationResultDto(
        UUID validationId,
        UUID ticketId,
        ValidationResult result,
        FailureReason failureReason,
        boolean isOffline,
        Instant attemptedAt,
        Instant syncedAt,
        boolean conflictDetected
) {}