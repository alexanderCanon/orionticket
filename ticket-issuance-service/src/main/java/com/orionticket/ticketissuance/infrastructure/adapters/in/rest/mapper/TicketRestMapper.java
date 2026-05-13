package com.orionticket.ticketissuance.infrastructure.adapters.in.rest.mapper;

import com.orionticket.ticketissuance.application.port.in.command.IssueTicketCommand;
import com.orionticket.ticketissuance.domain.model.Ticket;
import com.orionticket.ticketissuance.infrastructure.adapters.in.rest.dto.IssueTicketRequest;
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
                ticket.issuedAt(),
                ticket.deliveryChannels()
        );
    }

    public IssueTicketCommand toCommand(IssueTicketRequest request) {
        return new IssueTicketCommand(
                request.orderId(),
                request.buyerId(),
                request.eventId(),
                request.dateId(),
                request.seatId(),
                request.type(),
                request.holderName(),
                request.qrCode(),
                request.qrExpiresAt(),
                request.accessPolicy(),
                request.deliveryChannels()
        );
    }
}
