package com.orionticket.ticketissuance.application.port.out;

import com.orionticket.ticketissuance.domain.model.Ticket;

public interface TicketEventPublisherPort {
    void publishTicketIssuedEvent(Ticket ticket);
}
