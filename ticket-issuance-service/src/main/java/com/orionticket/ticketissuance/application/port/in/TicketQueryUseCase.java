package com.orionticket.ticketissuance.application.port.in;

import com.orionticket.ticketissuance.domain.model.Ticket;

import java.util.List;
import java.util.UUID;

public interface TicketQueryUseCase {

    Ticket getTicket(UUID ticketId);

    List<Ticket> listBuyerTickets(UUID buyerId, int page, int size);
}
