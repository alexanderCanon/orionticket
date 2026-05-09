package com.orionticket.ticketissuance.infrastructure.adapters.out.persistence.mapper;

import com.orionticket.ticketissuance.domain.model.Ticket;
import com.orionticket.ticketissuance.domain.model.TicketStatus;
import com.orionticket.ticketissuance.domain.model.TicketType;
import com.orionticket.ticketissuance.infrastructure.adapters.out.persistence.entity.TicketEntity;
import org.springframework.stereotype.Component;

@Component
public class TicketPersistenceMapper {

    public Ticket toDomain(TicketEntity entity) {
        return new Ticket(
                entity.getTicketId(),
                entity.getOrderId(),
                entity.getBuyerId(),
                entity.getEventId(),
                entity.getDateId(),
                entity.getSeatId(),
                TicketType.valueOf(entity.getType()),
                entity.getHolderName(),
                entity.getQrCode(),
                entity.getQrExpiresAt(),
                entity.getAccessPolicy(),
                TicketStatus.valueOf(entity.getStatus()),
                entity.getDeliveredAt(),
                entity.getIssuedAt()
        );
    }
}
