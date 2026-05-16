package com.orionticket.orders.order.domain.exception;

import java.util.UUID;

// No se encontró snapshot de la reserva — posiblemente el evento ReservationCreated no llegó aún
public class ReservationSnapshotNotFoundException extends RuntimeException {
    public ReservationSnapshotNotFoundException(UUID reservationId) {
        super("Reservation not found or expired: " + reservationId);
    }
}
