package com.orionticket.acesscontrol.infrastructure.adapters.out.persistence.repository;

import com.orionticket.acesscontrol.domain.model.ValidationRecord;
import com.orionticket.acesscontrol.domain.port.out.ValidationRecordRepositoryPort;
import com.orionticket.acesscontrol.infrastructure.adapters.out.persistence.mapper.ValidationRecordEntityMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class ValidationRecordRepositoryImpl implements ValidationRecordRepositoryPort {

    private final ValidationRecordRepository jpaRepository;
    private final ValidationRecordEntityMapper mapper;

    public ValidationRecordRepositoryImpl(
            ValidationRecordRepository jpaRepository,
            ValidationRecordEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public ValidationRecord save(ValidationRecord record) {
        var entity = mapper.toEntity(record);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<ValidationRecord> saveAll(List<ValidationRecord> records) {
        List<ValidationRecordEntity> entities = records.stream().map(mapper::toEntity).toList();
        List<ValidationRecordEntity> saved = jpaRepository.saveAll(entities);
        return saved.stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<ValidationRecord> findFirstByTicketIdAndResult(UUID ticketId, String result) {
        return jpaRepository.findFirstByTicketIdAndResult(ticketId, result)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByTicketIdAndResult(UUID ticketId, String result) {
        return jpaRepository.existsByTicketIdAndResult(ticketId, result);
    }

    @Override
    public List<ValidationRecord> findByEventIdAndDateId(UUID eventId, UUID dateId) {
        return jpaRepository.findByEventIdAndDateId(eventId, dateId)
                .stream().map(mapper::toDomain).toList();
    }
}