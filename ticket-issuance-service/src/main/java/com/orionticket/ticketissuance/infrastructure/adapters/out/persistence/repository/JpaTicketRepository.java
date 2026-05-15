package com.orionticket.ticketissuance.infrastructure.adapters.out.persistence.repository;

import com.orionticket.ticketissuance.infrastructure.adapters.out.persistence.entity.TicketEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaTicketRepository extends JpaRepository<TicketEntity, UUID> {

    List<TicketEntity> findByBuyerId(UUID buyerId, Pageable pageable);
}
