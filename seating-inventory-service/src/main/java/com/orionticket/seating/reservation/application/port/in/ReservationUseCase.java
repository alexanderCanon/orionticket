package com.orionticket.seating.reservation.application.port.in;

import com.orionticket.seating.reservation.domain.model.Reservation;

import java.util.UUID;

public interface ReservationUseCase {

    Reservation createReservation(UUID seatId, UUID buyerId,
                                  UUID eventId, UUID dateId, UUID batchId);

    Reservation releaseReservation(UUID reservationId);
}
