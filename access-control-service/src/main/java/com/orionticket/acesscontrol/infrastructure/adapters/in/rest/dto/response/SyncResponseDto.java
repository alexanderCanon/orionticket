package com.orionticket.acesscontrol.infrastructure.adapters.in.rest.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record SyncResponseDto(
        String validatorDeviceId,
        int totalSynced,
        List<SyncResultResponseDto> results,
        @JsonProperty("conflictsDetected") int conflictsDetected
) {}