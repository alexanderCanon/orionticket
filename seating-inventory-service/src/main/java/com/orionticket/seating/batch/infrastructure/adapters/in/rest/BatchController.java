package com.orionticket.seating.batch.infrastructure.adapters.in.rest;

import com.orionticket.seating.batch.application.port.in.BatchManagementUseCase;
import com.orionticket.seating.batch.domain.model.Batch;
import com.orionticket.seating.batch.infrastructure.adapters.in.rest.dto.BatchResponse;
import com.orionticket.seating.batch.infrastructure.adapters.in.rest.dto.CreateBatchRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/events/{eventId}/dates/{dateId}/batches")
@RequiredArgsConstructor
@Tag(name = "Batches", description = "Sales batch configuration and query endpoints")
public class BatchController {

    private final BatchManagementUseCase batchManagementUseCase;

    @Operation(summary = "Create batch", description = "Creates a sales batch with price, capacity, and schedule.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Batch created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Batch conflicts with existing inventory rules")
    })
    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<BatchResponse> createBatch(
            @PathVariable UUID eventId,
            @PathVariable UUID dateId,
            @Valid @RequestBody CreateBatchRequest request) {

        Batch batch = batchManagementUseCase.createBatch(
                eventId, dateId,
                request.getName(), request.getPrice(),
                request.getCurrency(), request.getCapacity(),
                request.getScheduledStartAt());

        return ResponseEntity.status(HttpStatus.CREATED).body(BatchResponse.from(batch));
    }

    @Operation(summary = "List batches", description = "Returns batches configured for an event date.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Batches returned")
    })
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BatchResponse>> getBatches(
            @PathVariable UUID eventId,
            @PathVariable UUID dateId) {

        List<BatchResponse> batches = batchManagementUseCase.getBatchesByEventAndDate(eventId, dateId)
                .stream().map(BatchResponse::from).collect(Collectors.toList());

        return ResponseEntity.ok(batches);
    }
}
