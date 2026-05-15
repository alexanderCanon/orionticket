package com.orionticket.acesscontrol.infrastructure.adapters.in.rest.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SyncRequestDto(
        @NotNull String validatorDeviceId,
        @NotNull UUID eventId,
        @NotNull UUID dateId,
        @NotEmpty @Valid List<OfflineRecordDto> records
) {
    public record OfflineRecordDto(
            @NotNull UUID ticketId,
            @NotNull Instant attemptedAt
    ) {}
}