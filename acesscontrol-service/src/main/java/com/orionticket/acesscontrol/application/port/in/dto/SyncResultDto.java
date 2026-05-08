package com.orionticket.acesscontrol.application.port.in.dto;

import java.util.List;

public record SyncResultDto(
        String validatorDeviceId,
        int totalSynced,
        List<ValidationResultDto> results,
        int conflictsDetected
) {}