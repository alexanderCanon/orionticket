package com.orionticket.seating.batch.infrastructure.adapters.out.persistence.repository;

import com.orionticket.seating.batch.infrastructure.adapters.out.persistence.entity.BatchJpaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataBatchRepository extends JpaRepository<BatchJpaEntity, UUID> {

    List<BatchJpaEntity> findByEventIdAndDateId(UUID eventId, UUID dateId);

    // SELECT ... FOR UPDATE: bloquea la fila hasta el fin de la transacción.
    // Necesario en ReservationService para que dos reservas del mismo batch
    // no lean el mismo valor de `sold` al mismo tiempo.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM BatchJpaEntity b WHERE b.batchId = :batchId")
    Optional<BatchJpaEntity> findByIdWithLock(@Param("batchId") UUID batchId);

    // Para el scheduler que activa tandas: busca las que tienen status SCHEDULED
    // y cuyo scheduledStartAt ya pasó.
    @Query("SELECT b FROM BatchJpaEntity b WHERE b.status = 'SCHEDULED' AND b.scheduledStartAt <= :now")
    List<BatchJpaEntity> findScheduledBatchesDue(@Param("now") ZonedDateTime now);
}
