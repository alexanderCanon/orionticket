package com.orionticket.acesscontrol.domain.port.out;

import com.orionticket.acesscontrol.domain.model.ValidationRecord;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ValidationRecordRepositoryPort {
    ValidationRecord save(ValidationRecord record);
    List<ValidationRecord> saveAll(List<ValidationRecord> records);
    Optional<ValidationRecord> findFirstByTicketIdAndResult(UUID ticketId, String result);
    boolean existsByTicketIdAndResult(UUID ticketId, String result);
    List<ValidationRecord> findByEventIdAndDateId(UUID eventId, UUID dateId);
}