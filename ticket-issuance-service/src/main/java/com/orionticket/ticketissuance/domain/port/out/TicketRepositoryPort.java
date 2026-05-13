package com.orionticket.ticketissuance.domain.port.out;

import com.orionticket.ticketissuance.domain.model.Ticket;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketRepositoryPort {

    Optional<Ticket> findById(UUID ticketId);

    List<Ticket> findByBuyerId(UUID buyerId, int page, int size);

    Ticket save(Ticket ticket);

    Optional<Ticket> update(Ticket ticket);
}
