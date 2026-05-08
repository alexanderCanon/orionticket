package com.orionticket.acesscontrol.infrastructure.adapters.out.persistence;

import com.orionticket.acesscontrol.base.IntegrationTestBase;
import com.orionticket.acesscontrol.domain.model.FailureReason;
import com.orionticket.acesscontrol.domain.model.ValidationRecord;
import com.orionticket.acesscontrol.domain.model.ValidationResult;
import com.orionticket.acesscontrol.infrastructure.adapters.out.persistence.mapper.ValidationRecordEntityMapper;
import com.orionticket.acesscontrol.infrastructure.adapters.out.persistence.repository.ValidationRecordRepositoryImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.orionticket.acesscontrol.infrastructure.adapters.out.persistence.repository.ValidationRecordRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationRecordRepositoryTest extends IntegrationTestBase {

    @Autowired
    private ValidationRecordRepository jpaRepository;

    @Autowired
    private ValidationRecordEntityMapper mapper;

    private ValidationRecordRepositoryImpl repository;

    @Test
    @DisplayName("Save and retrieve ValidationRecord")
    void save_shouldPersistAndRetrieve() {
        repository = new ValidationRecordRepositoryImpl(jpaRepository, mapper);

        ValidationRecord record = ValidationRecord.create(
                UUID.randomUUID(),
                "device-001",
                UUID.randomUUID(),
                UUID.randomUUID(),
                ValidationResult.SUCCEEDED,
                null,
                false
        );

        ValidationRecord saved = repository.save(record);

        assertThat(saved).isNotNull();
        assertThat(saved.validationId()).isNotNull();
        assertThat(saved.ticketId()).isEqualTo(record.ticketId());
        assertThat(saved.result()).isEqualTo(ValidationResult.SUCCEEDED);
    }

    @Test
    @DisplayName("Find first by ticketId and result")
    void findFirstByTicketIdAndResult_shouldFindExisting() {
        repository = new ValidationRecordRepositoryImpl(jpaRepository, mapper);

        ValidationRecord record = ValidationRecord.create(
                UUID.randomUUID(),
                "device-001",
                UUID.randomUUID(),
                UUID.randomUUID(),
                ValidationResult.SUCCEEDED,
                null,
                false
        );

        repository.save(record);

        Optional<ValidationRecord> found = repository.findFirstByTicketIdAndResult(
                record.ticketId(), "SUCCEEDED");

        assertThat(found).isPresent();
        assertThat(found.get().ticketId()).isEqualTo(record.ticketId());
    }

    @Test
    @DisplayName("Save multiple records")
    void saveAll_shouldPersistMultiple() {
        repository = new ValidationRecordRepositoryImpl(jpaRepository, mapper);

        List<ValidationRecord> records = List.of(
                ValidationRecord.create(UUID.randomUUID(), "device-001",
                        UUID.randomUUID(), UUID.randomUUID(), ValidationResult.SUCCEEDED, null, true),
                ValidationRecord.create(UUID.randomUUID(), "device-001",
                        UUID.randomUUID(), UUID.randomUUID(), ValidationResult.FAILED,
                        FailureReason.WRONG_EVENT, true)
        );

        List<ValidationRecord> saved = repository.saveAll(records);

        assertThat(saved).hasSize(2);
        assertThat(saved).allMatch(r -> r.validationId() != null);
    }

    @Test
    @DisplayName("Find by eventId and dateId")
    void findByEventIdAndDateId_shouldFindRecords() {
        repository = new ValidationRecordRepositoryImpl(jpaRepository, mapper);

        UUID eventId = UUID.randomUUID();
        UUID dateId = UUID.randomUUID();

        ValidationRecord record = ValidationRecord.create(
                UUID.randomUUID(),
                "device-001",
                eventId,
                dateId,
                ValidationResult.SUCCEEDED,
                null,
                false
        );

        repository.save(record);

        List<ValidationRecord> found = repository.findByEventIdAndDateId(eventId, dateId);

        assertThat(found).hasSize(1);
        assertThat(found.get(0).eventId()).isEqualTo(eventId);
        assertThat(found.get(0).dateId()).isEqualTo(dateId);
    }
}