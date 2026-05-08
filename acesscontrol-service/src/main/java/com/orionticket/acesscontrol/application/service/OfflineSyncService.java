package com.orionticket.acesscontrol.application.service;

import com.orionticket.acesscontrol.application.port.in.command.SyncValidationsCommand;
import com.orionticket.acesscontrol.application.port.in.dto.SyncResultDto;
import com.orionticket.acesscontrol.application.port.in.dto.ValidationResultDto;
import com.orionticket.acesscontrol.domain.model.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OfflineSyncService {
    private static final Logger log = LoggerFactory.getLogger(OfflineSyncService.class);

    public SyncResultDto syncValidations(SyncValidationsCommand command) {
        log.info("Received offline sync: device={}, eventId={}, dateId={}, records={}",
                command.validatorDeviceId(), command.eventId(), command.dateId(), command.records().size());

        List<ValidationResultDto> results = command.records().stream()
                .map(record -> new ValidationResultDto(
                        UUID.randomUUID(),
                        record.ticketId(),
                        ValidationResult.SUCCEEDED,
                        null,
                        true,
                        record.attemptedAt(),
                        Instant.now(),
                        false
                ))
                .collect(Collectors.toList());

        return new SyncResultDto(
                command.validatorDeviceId(),
                results.size(),
                results,
                0
        );
    }
}