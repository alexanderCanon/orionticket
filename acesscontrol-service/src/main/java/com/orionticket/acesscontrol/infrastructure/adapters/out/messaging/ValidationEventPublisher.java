package com.orionticket.acesscontrol.infrastructure.adapters.out.messaging.event;

import com.orionticket.acesscontrol.domain.model.ValidationRecord;
import com.orionticket.acesscontrol.domain.port.out.DomainEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ValidationEventPublisher implements DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ValidationEventPublisher.class);

    @Override
    public void publish(ValidationRecord record) {
        log.info("Publishing ValidationRecord event: validationId={}, ticketId={}, result={}",
                record.validationId(), record.ticketId(), record.result());
    }

    @Override
    public void publishAll(List<ValidationRecord> records) {
        log.info("Publishing {} ValidationRecord events", records.size());
    }
}