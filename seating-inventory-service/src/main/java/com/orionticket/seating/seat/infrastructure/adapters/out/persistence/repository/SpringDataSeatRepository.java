package com.orionticket.seating.seat.infrastructure.adapters.out.persistence.repository;

import com.orionticket.seating.seat.infrastructure.adapters.out.persistence.entity.SeatJpaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataSeatRepository extends JpaRepository<SeatJpaEntity, UUID> {

    // JOIN FETCH batch: trae el batch en la misma query para evitar N+1.
    // Sin este fetch, acceder a seat.getBatch() dispararía una query adicional por cada seat.
    @Query("SELECT s FROM SeatJpaEntity s JOIN FETCH s.batch WHERE s.eventId = :eventId AND s.dateId = :dateId")
    List<SeatJpaEntity> findByEventIdAndDateId(@Param("eventId") UUID eventId,
                                               @Param("dateId") UUID dateId);

    @Query("SELECT s FROM SeatJpaEntity s JOIN FETCH s.batch WHERE s.eventId = :eventId AND s.dateId = :dateId AND s.zone = :zone")
    List<SeatJpaEntity> findByEventIdAndDateIdAndZone(@Param("eventId") UUID eventId,
                                                       @Param("dateId") UUID dateId,
                                                       @Param("zone") String zone);

    @Query("SELECT s FROM SeatJpaEntity s JOIN FETCH s.batch WHERE s.eventId = :eventId AND s.dateId = :dateId AND s.zone = :zone AND s.section = :section")
    List<SeatJpaEntity> findByEventIdAndDateIdAndZoneAndSection(@Param("eventId") UUID eventId,
                                                                 @Param("dateId") UUID dateId,
                                                                 @Param("zone") String zone,
                                                                 @Param("section") String section);

    // SELECT ... FOR UPDATE: primera línea de defensa contra overbooking.
    // Ver ReservationService para la explicación completa del flujo de locks.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SeatJpaEntity s WHERE s.seatId = :seatId")
    Optional<SeatJpaEntity> findByIdWithLock(@Param("seatId") UUID seatId);
}
