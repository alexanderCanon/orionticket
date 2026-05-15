package com.orionticket.seating.seat.domain.port.out;

import com.orionticket.seating.seat.domain.model.Seat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SeatRepositoryPort {

    Seat save(Seat seat);

    List<Seat> saveAll(List<Seat> seats);

    Optional<Seat> findById(UUID seatId);

    // Con lock pesimista (SELECT FOR UPDATE): garantiza que solo un thread a la vez
    // pueda leer y modificar el estado de este asiento en la ventana de la transacción.
    Optional<Seat> findByIdWithLock(UUID seatId);

    List<Seat> findByEventIdAndDateId(UUID eventId, UUID dateId);

    List<Seat> findByEventIdAndDateIdAndZone(UUID eventId, UUID dateId, String zone);

    List<Seat> findByEventIdAndDateIdAndZoneAndSection(UUID eventId, UUID dateId,
                                                        String zone, String section);
}
