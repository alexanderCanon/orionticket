package com.orionticket.acesscontrol.application.service;

import com.orionticket.acesscontrol.application.port.in.command.ValidateTicketCommand;
import com.orionticket.acesscontrol.application.port.in.dto.ValidationResultDto;
import com.orionticket.acesscontrol.domain.model.FailureReason;
import com.orionticket.acesscontrol.domain.model.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.UUID;

@Service
public class ValidationApplicationService {
    private static final Logger log = LoggerFactory.getLogger(ValidationApplicationService.class);

    public ValidationResultDto validateTicket(ValidateTicketCommand command) {
        log.info("Received validation request: ticketId={}, device={}, eventId={}, dateId={}",
                command.ticketId(), command.validatorDeviceId(), command.eventId(), command.dateId());

        return new ValidationResultDto(
                UUID.randomUUID(),
                command.ticketId(),
                ValidationResult.SUCCEEDED,
                null,
                false,
                Instant.now(),
                null,
                false
        );
    }
}