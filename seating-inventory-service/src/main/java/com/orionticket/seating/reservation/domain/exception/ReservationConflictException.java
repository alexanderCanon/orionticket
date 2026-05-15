package com.orionticket.seating.reservation.domain.exception;

import java.util.UUID;

// Lanzada cuando el asiento ya está RESERVED o SOLD. Mapea a HTTP 409 en GlobalExceptionHandler.
public class ReservationConflictException extends RuntimeException {
    public ReservationConflictException(UUID seatId) {
        super("Seat is not available: " + seatId);
    }
}
