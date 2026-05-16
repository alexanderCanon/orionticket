package com.orionticket.orders.order.domain.port.out;

import com.orionticket.orders.order.domain.model.ReservationSnapshot;

import java.util.Optional;
import java.util.UUID;

public interface ReservationSnapshotRepositoryPort {
    ReservationSnapshot save(ReservationSnapshot snapshot);
    Optional<ReservationSnapshot> findById(UUID reservationId);
}
