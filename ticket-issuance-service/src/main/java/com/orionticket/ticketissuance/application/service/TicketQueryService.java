package com.orionticket.ticketissuance.application.service;

import com.orionticket.ticketissuance.application.port.in.TicketQueryUseCase;
import com.orionticket.ticketissuance.domain.exception.TicketNotFoundException;
import com.orionticket.ticketissuance.domain.model.Ticket;
import com.orionticket.ticketissuance.domain.port.out.TicketRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TicketQueryService implements TicketQueryUseCase {

    private final TicketRepositoryPort ticketRepository;

    public TicketQueryService(TicketRepositoryPort ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public Ticket getTicket(UUID ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));
    }

    @Override
    public List<Ticket> listBuyerTickets(UUID buyerId, int page, int size) {
        return ticketRepository.findByBuyerId(buyerId, page, size);
    }
}
