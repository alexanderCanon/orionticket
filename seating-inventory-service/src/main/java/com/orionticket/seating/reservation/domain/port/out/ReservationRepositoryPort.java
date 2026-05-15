package com.orionticket.seating.reservation.domain.port.out;

import com.orionticket.seating.reservation.domain.model.Reservation;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepositoryPort {

    Reservation save(Reservation reservation);

    Optional<Reservation> findById(UUID reservationId);

    // Para el job de expiración: busca todas las reservas ACTIVE cuyo expires_at <= now.
    // Esta query usa el índice idx_reservations_expires para ser eficiente.
    List<Reservation> findActiveExpiredBefore(ZonedDateTime cutoff);

    Optional<Reservation> findActiveByBuyerIdAndSeatId(UUID buyerId, UUID seatId);
}
