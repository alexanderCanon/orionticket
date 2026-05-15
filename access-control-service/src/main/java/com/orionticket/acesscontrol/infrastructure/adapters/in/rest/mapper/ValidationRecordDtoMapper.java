package com.orionticket.acesscontrol.infrastructure.adapters.in.rest.mapper;

import com.orionticket.acesscontrol.application.port.in.dto.SyncResultDto;
import com.orionticket.acesscontrol.application.port.in.dto.ValidationResultDto;
import com.orionticket.acesscontrol.infrastructure.adapters.in.rest.dto.response.SyncResponseDto;
import com.orionticket.acesscontrol.infrastructure.adapters.in.rest.dto.response.SyncResultResponseDto;
import com.orionticket.acesscontrol.infrastructure.adapters.in.rest.dto.response.ValidationResponseDto;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ValidationRecordDtoMapper {

    public ValidationResponseDto toResponseDto(ValidationResultDto dto) {
        String result = dto.result() != null ? dto.result().name() : null;
        String failureReason = dto.failureReason() != null ? dto.failureReason().name() : null;
        return new ValidationResponseDto(
                dto.validationId(),
                dto.ticketId(),
                result,
                failureReason,
                dto.isOffline(),
                dto.attemptedAt()
        );
    }

    public SyncResponseDto toSyncResponseDto(SyncResultDto dto) {
        List<SyncResultResponseDto> results = dto.results().stream()
                .map(this::toSyncResultResponseDto)
                .collect(Collectors.toList());

        return new SyncResponseDto(
                dto.validatorDeviceId(),
                dto.totalSynced(),
                results,
                dto.conflictsDetected()
        );
    }

    private SyncResultResponseDto toSyncResultResponseDto(ValidationResultDto dto) {
        String result = dto.result() != null ? dto.result().name() : null;
        String failureReason = dto.failureReason() != null ? dto.failureReason().name() : null;
        return new SyncResultResponseDto(
                dto.validationId(),
                dto.ticketId(),
                result,
                failureReason,
                dto.conflictDetected(),
                dto.syncedAt()
        );
    }
}