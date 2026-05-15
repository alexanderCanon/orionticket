package com.orionticket.seating.reservation.domain.exception;

import java.util.UUID;

// Lanzada cuando el batch está EXHAUSTED (sold == capacity) o no está ACTIVE.
// Mapea a HTTP 410 GONE en GlobalExceptionHandler: el cupo es definitivamente agotado.
public class BatchExhaustedException extends RuntimeException {
    public BatchExhaustedException(UUID batchId) {
        super("Batch is exhausted or not active: " + batchId);
    }
}
