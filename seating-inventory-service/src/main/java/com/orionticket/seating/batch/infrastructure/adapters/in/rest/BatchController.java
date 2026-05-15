package com.orionticket.seating.batch.infrastructure.adapters.in.rest;

import com.orionticket.seating.batch.application.port.in.BatchManagementUseCase;
import com.orionticket.seating.batch.domain.model.Batch;
import com.orionticket.seating.batch.infrastructure.adapters.in.rest.dto.BatchResponse;
import com.orionticket.seating.batch.infrastructure.adapters.in.rest.dto.CreateBatchRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/events/{eventId}/dates/{dateId}/batches")
@RequiredArgsConstructor
public class BatchController {

    private final BatchManagementUseCase batchManagementUseCase;

    @PostMapping
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

    @GetMapping
    public ResponseEntity<List<BatchResponse>> getBatches(
            @PathVariable UUID eventId,
            @PathVariable UUID dateId) {

        List<BatchResponse> batches = batchManagementUseCase.getBatchesByEventAndDate(eventId, dateId)
                .stream().map(BatchResponse::from).collect(Collectors.toList());

        return ResponseEntity.ok(batches);
    }
}
