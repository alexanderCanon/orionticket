package com.orionticket.ticketissuance.application.service;

import com.orionticket.ticketissuance.application.port.in.InvalidateTicketUseCase;
import com.orionticket.ticketissuance.application.port.in.command.InvalidateTicketCommand;
import com.orionticket.ticketissuance.domain.exception.TicketNotFoundException;
import com.orionticket.ticketissuance.domain.model.Ticket;
import com.orionticket.ticketissuance.domain.model.TicketStatus;
import com.orionticket.ticketissuance.domain.port.out.TicketRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InvalidateTicketService implements InvalidateTicketUseCase {

    private final TicketRepositoryPort ticketRepository;

    public InvalidateTicketService(TicketRepositoryPort ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    @Transactional
    public Ticket invalidateTicket(InvalidateTicketCommand command) {
        Ticket existingTicket = ticketRepository.findById(command.ticketId())
                .orElseThrow(() -> new TicketNotFoundException(command.ticketId()));

        // Create a new Ticket instance with updated status (immutability)
        Ticket invalidatedTicket = new Ticket(
                existingTicket.ticketId(),
                existingTicket.orderId(),
                existingTicket.buyerId(),
                existingTicket.eventId(),
                existingTicket.dateId(),
                existingTicket.seatId(),
                existingTicket.type(),
                existingTicket.holderName(),
                existingTicket.qrCode(),
                existingTicket.qrExpiresAt(),
                existingTicket.accessPolicy(),
                TicketStatus.INVALIDATED, // Update status to INVALIDATED
                existingTicket.deliveredAt(),
                existingTicket.issuedAt(),
                existingTicket.deliveryChannels()
        );

        return ticketRepository.update(invalidatedTicket)
                .orElseThrow(() -> new IllegalStateException("Failed to update ticket with ID: " + command.ticketId()));
    }
}
