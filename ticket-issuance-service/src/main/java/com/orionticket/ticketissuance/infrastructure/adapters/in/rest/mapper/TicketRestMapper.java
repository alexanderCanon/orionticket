package com.orionticket.ticketissuance.infrastructure.adapters.in.rest.mapper;

import com.orionticket.ticketissuance.domain.model.Ticket;
import com.orionticket.ticketissuance.infrastructure.adapters.in.rest.dto.TicketResponse;
import org.springframework.stereotype.Component;

@Component
public class TicketRestMapper {

    public TicketResponse toResponse(Ticket ticket) {
        return new TicketResponse(
                ticket.ticketId(),
                ticket.orderId(),
                ticket.buyerId(),
                ticket.eventId(),
                ticket.dateId(),
                ticket.seatId(),
                ticket.type().name(),
                ticket.holderName(),
                ticket.qrCode(),
                ticket.qrExpiresAt(),
                ticket.accessPolicy(),
                ticket.status().name(),
                ticket.issuedAt()
        );
    }
}
