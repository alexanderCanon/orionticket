package com.orionticket.acesscontrol.infrastructure.adapters.out.persistence.mapper;

import com.orionticket.acesscontrol.domain.model.FailureReason;
import com.orionticket.acesscontrol.domain.model.ValidationRecord;
import com.orionticket.acesscontrol.domain.model.ValidationResult;
import com.orionticket.acesscontrol.infrastructure.adapters.out.persistence.entity.ValidationRecordEntity;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class ValidationRecordEntityMapper {

    public ValidationRecord toDomain(ValidationRecordEntity entity) {
        ValidationResult result = entity.getResult() != null
                ? ValidationResult.valueOf(entity.getResult())
                : null;
        FailureReason failureReason = entity.getFailureReason() != null
                ? FailureReason.valueOf(entity.getFailureReason())
                : null;

        return new ValidationRecord(
                entity.getValidationId(),
                entity.getTicketId(),
                entity.getValidatorDeviceId(),
                entity.getEventId(),
                entity.getDateId(),
                entity.getAttemptedAt(),
                result,
                failureReason,
                entity.isOffline(),
                entity.getSyncedAt(),
                entity.isConflictDetected()
        );
    }

    public ValidationRecordEntity toEntity(ValidationRecord domain) {
        ValidationRecordEntity entity = new ValidationRecordEntity();
        entity.setValidationId(domain.validationId());
        entity.setTicketId(domain.ticketId());
        entity.setValidatorDeviceId(domain.validatorDeviceId());
        entity.setEventId(domain.eventId());
        entity.setDateId(domain.dateId());
        entity.setAttemptedAt(domain.attemptedAt());
        entity.setResult(domain.result() != null ? domain.result().name() : null);
        entity.setFailureReason(domain.failureReason() != null ? domain.failureReason().name() : null);
        entity.setOffline(domain.isOffline());
        entity.setSyncedAt(domain.syncedAt());
        entity.setConflictDetected(domain.conflictDetected());
        entity.setCreatedAt(Instant.now());
        return entity;
    }
}