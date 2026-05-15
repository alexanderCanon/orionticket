package com.orionticket.acesscontrol.infrastructure.adapters.in.rest.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ValidationRequestDto(
        @NotNull UUID ticketId,
        @NotNull String validatorDeviceId,
        @NotNull UUID eventId,
        @NotNull UUID dateId
) {}