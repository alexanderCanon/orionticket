package com.orionticket.seating.batch.infrastructure.adapters.out.persistence;

import com.orionticket.seating.batch.domain.model.Batch;
import com.orionticket.seating.batch.domain.port.out.BatchRepositoryPort;
import com.orionticket.seating.batch.infrastructure.adapters.out.persistence.mapper.BatchPersistenceMapper;
import com.orionticket.seating.batch.infrastructure.adapters.out.persistence.repository.SpringDataBatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

// Adapter de salida: implementa BatchRepositoryPort (puerto del dominio) usando JPA.
// El dominio no sabe que existe Spring Data; solo conoce la interfaz del puerto.
@Component
@RequiredArgsConstructor
public class BatchRepositoryAdapter implements BatchRepositoryPort {

    private final SpringDataBatchRepository springDataBatchRepository;
    private final BatchPersistenceMapper mapper;

    @Override
    public Batch save(Batch batch) {
        return mapper.toDomain(springDataBatchRepository.save(mapper.toEntity(batch)));
    }

    @Override
    public Optional<Batch> findById(UUID batchId) {
        return springDataBatchRepository.findById(batchId).map(mapper::toDomain);
    }

    @Override
    public Optional<Batch> findByIdWithLock(UUID batchId) {
        return springDataBatchRepository.findByIdWithLock(batchId).map(mapper::toDomain);
    }

    @Override
    public List<Batch> findByEventIdAndDateId(UUID eventId, UUID dateId) {
        return springDataBatchRepository.findByEventIdAndDateId(eventId, dateId)
                .stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Batch> findScheduledBatchesDue() {
        return springDataBatchRepository.findScheduledBatchesDue(ZonedDateTime.now())
                .stream().map(mapper::toDomain).collect(Collectors.toList());
    }
}
