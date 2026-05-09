package com.orionticket.ticketissuance.infrastructure.adapters.out.persistence;

import com.orionticket.ticketissuance.domain.model.Ticket;
import com.orionticket.ticketissuance.domain.port.out.TicketRepositoryPort;
import com.orionticket.ticketissuance.infrastructure.adapters.out.persistence.mapper.TicketPersistenceMapper;
import com.orionticket.ticketissuance.infrastructure.adapters.out.persistence.repository.JpaTicketRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class TicketPersistenceAdapter implements TicketRepositoryPort {

    private final JpaTicketRepository jpaTicketRepository;
    private final TicketPersistenceMapper ticketPersistenceMapper;

    public TicketPersistenceAdapter(
            JpaTicketRepository jpaTicketRepository,
            TicketPersistenceMapper ticketPersistenceMapper
    ) {
        this.jpaTicketRepository = jpaTicketRepository;
        this.ticketPersistenceMapper = ticketPersistenceMapper;
    }

    @Override
    public Optional<Ticket> findById(UUID ticketId) {
        return jpaTicketRepository.findById(ticketId).map(ticketPersistenceMapper::toDomain);
    }

    @Override
    public List<Ticket> findByBuyerId(UUID buyerId, int page, int size) {
        return jpaTicketRepository.findByBuyerId(buyerId, PageRequest.of(page, size)).stream()
                .map(ticketPersistenceMapper::toDomain)
                .toList();
    }
}
