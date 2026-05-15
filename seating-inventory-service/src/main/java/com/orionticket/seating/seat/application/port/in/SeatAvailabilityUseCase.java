package com.orionticket.seating.seat.application.port.in;

import com.orionticket.seating.seat.domain.model.Seat;

import java.util.List;
import java.util.UUID;

public interface SeatAvailabilityUseCase {

    // Devuelve todos los asientos de un evento+fecha, opcionalmente filtrados por zona y sección.
    // El Controller de disponibilidad usa este use case para el GET /v1/events/{id}/dates/{id}/seats.
    List<Seat> getAvailableSeats(UUID eventId, UUID dateId, String zone, String section);
}
