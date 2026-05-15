package com.orionticket.seating.reservation.infrastructure.adapters.out.persistence.repository;

import com.orionticket.seating.reservation.infrastructure.adapters.out.persistence.entity.ReservationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataReservationRepository extends JpaRepository<ReservationJpaEntity, UUID> {

    // Para el job de expiración: usa el índice idx_reservations_expires para ser eficiente.
    // Solo retorna reservas ACTIVE (no procesa las que ya expiraron en ciclos anteriores).
    @Query("SELECT r FROM ReservationJpaEntity r WHERE r.status = 'ACTIVE' AND r.expiresAt <= :cutoff")
    List<ReservationJpaEntity> findActiveExpiredBefore(@Param("cutoff") ZonedDateTime cutoff);

    @Query("SELECT r FROM ReservationJpaEntity r WHERE r.buyerId = :buyerId AND r.seatId = :seatId AND r.status = 'ACTIVE'")
    Optional<ReservationJpaEntity> findActiveByBuyerIdAndSeatId(@Param("buyerId") UUID buyerId,
                                                                  @Param("seatId") UUID seatId);
}
