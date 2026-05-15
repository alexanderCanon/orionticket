package com.orionticket.ticketissuance.infrastructure.adapters.out.persistence.mapper;

import com.orionticket.ticketissuance.domain.model.DeliveryChannel;
import com.orionticket.ticketissuance.domain.model.Ticket;
import com.orionticket.ticketissuance.domain.model.TicketStatus;
import com.orionticket.ticketissuance.domain.model.TicketType;
import com.orionticket.ticketissuance.infrastructure.adapters.out.persistence.entity.TicketEntity;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

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
                entity.getIssuedAt(),
                entity.getDeliveryChannels().stream()
                        .map(DeliveryChannel::valueOf)
                        .collect(Collectors.toSet())
        );
    }

    public TicketEntity toEntity(Ticket domain) {
        TicketEntity entity = new TicketEntity();
        entity.setTicketId(domain.ticketId());
        entity.setOrderId(domain.orderId());
        entity.setBuyerId(domain.buyerId());
        entity.setEventId(domain.eventId());
        entity.setDateId(domain.dateId());
        entity.setSeatId(domain.seatId());
        entity.setType(domain.type().name()); // Convert enum to String
        entity.setHolderName(domain.holderName());
        entity.setQrCode(domain.qrCode());
        entity.setQrExpiresAt(domain.qrExpiresAt());
        entity.setAccessPolicy(domain.accessPolicy());
        entity.setStatus(domain.status().name()); // Convert enum to String
        entity.setDeliveredAt(domain.deliveredAt());
        entity.setIssuedAt(domain.issuedAt());
        entity.setDeliveryChannels(domain.deliveryChannels().stream()
                .map(Enum::name)
                .collect(Collectors.toSet()));
        return entity;
    }
}
