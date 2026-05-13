package com.orionticket.ticketissuance.application.service;

import com.orionticket.ticketissuance.application.port.in.IssueTicketUseCase;
import com.orionticket.ticketissuance.application.port.in.command.IssueTicketCommand;
import com.orionticket.ticketissuance.domain.model.Ticket;
import com.orionticket.ticketissuance.domain.model.TicketStatus;
import com.orionticket.ticketissuance.domain.port.out.TicketRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class IssueTicketService implements IssueTicketUseCase {

    private final TicketRepositoryPort ticketRepository;

    public IssueTicketService(TicketRepositoryPort ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    @Transactional
    public Ticket issueTicket(IssueTicketCommand command) {
        // Generate a new ticket ID
        UUID newTicketId = UUID.randomUUID();
        Instant issuedAt = Instant.now();

        // Create the Ticket domain object
        Ticket newTicket = new Ticket(
                newTicketId,
                command.orderId(),
                command.buyerId(),
                command.eventId(),
                command.dateId(),
                command.seatId(),
                command.type(),
                command.holderName(),
                command.qrCode(),
                command.qrExpiresAt(),
                command.accessPolicy(),
                TicketStatus.ISSUED, // Newly issued tickets start with ISSUED status
                null, // deliveredAt is null initially
                issuedAt,
                command.deliveryChannels()
        );

        // Save the new ticket
        return ticketRepository.save(newTicket);
    }
}
