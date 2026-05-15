package com.orionticket.acesscontrol.infrastructure.adapters.in.rest.controller;

import com.orionticket.acesscontrol.application.port.in.command.SyncValidationsCommand;
import com.orionticket.acesscontrol.application.port.in.command.ValidateTicketCommand;
import com.orionticket.acesscontrol.application.port.in.dto.SyncResultDto;
import com.orionticket.acesscontrol.application.port.in.dto.ValidationResultDto;
import com.orionticket.acesscontrol.application.service.OfflineSyncService;
import com.orionticket.acesscontrol.application.service.ValidationApplicationService;
import com.orionticket.acesscontrol.infrastructure.adapters.in.rest.dto.request.SyncRequestDto;
import com.orionticket.acesscontrol.infrastructure.adapters.in.rest.dto.request.ValidationRequestDto;
import com.orionticket.acesscontrol.infrastructure.adapters.in.rest.dto.response.SyncResponseDto;
import com.orionticket.acesscontrol.infrastructure.adapters.in.rest.dto.response.ValidationResponseDto;
import com.orionticket.acesscontrol.infrastructure.adapters.in.rest.mapper.ValidationRecordDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/validations")
@Tag(name = "Access Control", description = "Door validation and offline sync operations")
public class ValidationController {

    private final ValidationApplicationService validationService;
    private final OfflineSyncService offlineSyncService;
    private final ValidationRecordDtoMapper mapper;

    public ValidationController(
            ValidationApplicationService validationService,
            OfflineSyncService offlineSyncService,
            ValidationRecordDtoMapper mapper) {
        this.validationService = validationService;
        this.offlineSyncService = offlineSyncService;
        this.mapper = mapper;
    }

    @PostMapping
    @Operation(summary = "Validate ticket at door",
            description = "Synchronous, strong consistency. QR scan at the door. Must respond within 100 ms.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Validation result returned"),
            @ApiResponse(responseCode = "404", description = "Ticket not found"),
            @ApiResponse(responseCode = "422", description = "Missing/invalid fields")
    })
    public ResponseEntity<ValidationResponseDto> validateTicket(
            @Valid @RequestBody ValidationRequestDto request) {

        ValidateTicketCommand command = new ValidateTicketCommand(
                request.ticketId(),
                request.validatorDeviceId(),
                request.eventId(),
                request.dateId()
        );

        ValidationResultDto result = validationService.validateTicket(command);
        ValidationResponseDto response = mapper.toResponseDto(result);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/sync")
    @Operation(summary = "Sync offline validations",
            description = "Offline Validator device syncs queued scans after reconnection.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sync completed successfully"),
            @ApiResponse(responseCode = "422", description = "Invalid or empty records array")
    })
    public ResponseEntity<SyncResponseDto> syncValidations(
            @Valid @RequestBody SyncRequestDto request) {

        SyncValidationsCommand command = new SyncValidationsCommand(
                request.validatorDeviceId(),
                request.eventId(),
                request.dateId(),
                request.records().stream()
                        .map(r -> new SyncValidationsCommand.OfflineRecord(r.ticketId(), r.attemptedAt()))
                        .toList()
        );

        SyncResultDto result = offlineSyncService.syncValidations(command);
        SyncResponseDto response = mapper.toSyncResponseDto(result);

        return ResponseEntity.ok(response);
    }
}