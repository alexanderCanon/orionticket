package com.orionticket.acesscontrol.domain.port.out;

import com.orionticket.acesscontrol.domain.model.ValidationRecord;
import java.util.List;

public interface DomainEventPublisher {
    void publish(ValidationRecord record);
    void publishAll(List<ValidationRecord> records);
}