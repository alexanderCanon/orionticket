package com.orionticket.acesscontrol.application.service;

import com.orionticket.acesscontrol.application.port.in.command.ValidateTicketCommand;
import com.orionticket.acesscontrol.application.port.in.dto.ValidationResultDto;
import com.orionticket.acesscontrol.domain.exception.TicketNotFoundException;
import com.orionticket.acesscontrol.domain.model.FailureReason;
import com.orionticket.acesscontrol.domain.model.ValidationRecord;
import com.orionticket.acesscontrol.domain.model.ValidationResult;
import com.orionticket.acesscontrol.domain.port.out.DomainEventPublisher;
import com.orionticket.acesscontrol.domain.port.out.TicketLookupPort;
import com.orionticket.acesscontrol.domain.port.out.TicketLookupResult;
import com.orionticket.acesscontrol.domain.port.out.ValidationRecordRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ValidationApplicationService {
    private static final Logger log = LoggerFactory.getLogger(ValidationApplicationService.class);

    private final TicketLookupPort ticketLookupPort;
    private final ValidationRecordRepositoryPort validationRecordRepository;
    private final DomainEventPublisher domainEventPublisher;

    public ValidationApplicationService(
            TicketLookupPort ticketLookupPort,
            ValidationRecordRepositoryPort validationRecordRepository,
            DomainEventPublisher domainEventPublisher) {
        this.ticketLookupPort = ticketLookupPort;
        this.validationRecordRepository = validationRecordRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    public ValidationResultDto validateTicket(ValidateTicketCommand command) {
        log.info("Received validation request: ticketId={}, device={}, eventId={}, dateId={}",
                command.ticketId(), command.validatorDeviceId(), command.eventId(), command.dateId());

        TicketLookupResult ticket = ticketLookupPort.findTicketById(command.ticketId())
                .orElseThrow(() -> new TicketNotFoundException(command.ticketId().toString()));

        ValidationResult result = ValidationResult.SUCCEEDED;
        FailureReason failureReason = null;

        if (!ticket.eventId().equals(command.eventId()) || !ticket.dateId().equals(command.dateId())) {
            result = ValidationResult.FAILED;
            failureReason = FailureReason.WRONG_EVENT;
        } else if ("EXPIRED".equalsIgnoreCase(ticket.status())) {
            result = ValidationResult.FAILED;
            failureReason = FailureReason.EXPIRED;
        } else if ("INVALIDATED".equalsIgnoreCase(ticket.status())) {
            result = ValidationResult.FAILED;
            failureReason = FailureReason.INVALIDATED;
        } else if (validationRecordRepository.existsByTicketIdAndResult(command.ticketId(),
                ValidationResult.SUCCEEDED.name())) {
            result = ValidationResult.FAILED;
            failureReason = FailureReason.ALREADY_USED;
        }

        ValidationRecord record = ValidationRecord.create(
                command.ticketId(),
                command.validatorDeviceId(),
                command.eventId(),
                command.dateId(),
                result,
                failureReason,
                false);

        ValidationRecord savedRecord = validationRecordRepository.save(record);
        domainEventPublisher.publish(savedRecord);

        return new ValidationResultDto(
                savedRecord.validationId(),
                savedRecord.ticketId(),
                savedRecord.result(),
                savedRecord.failureReason(),
                savedRecord.isOffline(),
                savedRecord.attemptedAt(),
                savedRecord.syncedAt(),
                savedRecord.conflictDetected());
    }
}