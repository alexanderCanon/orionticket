package com.orionticket.seating.batch.domain.port.out;

import com.orionticket.seating.batch.domain.model.Batch;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Puerto de salida: define QUÉ necesita el dominio del repositorio, sin importar CÓMO
// está implementado (JPA, MongoDB, en memoria para tests). Sigue el Dependency Inversion Principle.
public interface BatchRepositoryPort {

    Batch save(Batch batch);

    Optional<Batch> findById(UUID batchId);

    // Con lock pesimista (SELECT FOR UPDATE): necesario en ReservationService
    // para garantizar que dos reservas concurrentes no lean el mismo sold y ambas pasen.
    Optional<Batch> findByIdWithLock(UUID batchId);

    List<Batch> findByEventIdAndDateId(UUID eventId, UUID dateId);

    // Para el scheduler que activa tandas cuyo scheduledStartAt ya pasó
    List<Batch> findScheduledBatchesDue();
}
