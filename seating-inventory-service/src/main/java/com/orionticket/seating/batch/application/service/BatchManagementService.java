package com.orionticket.seating.batch.application.service;

import com.orionticket.seating.batch.application.port.in.BatchManagementUseCase;
import com.orionticket.seating.batch.domain.model.Batch;
import com.orionticket.seating.batch.domain.port.out.BatchRepositoryPort;
import com.orionticket.seating.reservation.domain.port.out.DomainEventPublisherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BatchManagementService implements BatchManagementUseCase {

    private final BatchRepositoryPort batchRepository;
    private final DomainEventPublisherPort eventPublisher;

    @Override
    @Transactional
    public Batch createBatch(UUID eventId, UUID dateId, String name,
                             BigDecimal price, String currency,
                             int capacity, ZonedDateTime scheduledStartAt) {
        Batch batch = Batch.create(eventId, dateId, name, price, currency, capacity, scheduledStartAt);
        Batch saved = batchRepository.save(batch);

        // El evento se publica DESPUÉS del commit implícito de @Transactional.
        // POR QUÉ: si publicamos dentro de la transacción y esta hace rollback,
        // otros servicios ya habrían recibido un BatchCreated de un batch que no existe.
        // Spring llama al código después del return, ejecutando el commit antes de que
        // el llamador (Controller) haga algo más. No es garantía perfecta (ver Outbox Pattern),
        // pero es suficiente para el Checkpoint 1.
        eventPublisher.publishBatchCreated(saved);

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Batch> getBatchesByEventAndDate(UUID eventId, UUID dateId) {
        return batchRepository.findByEventIdAndDateId(eventId, dateId);
    }
}
