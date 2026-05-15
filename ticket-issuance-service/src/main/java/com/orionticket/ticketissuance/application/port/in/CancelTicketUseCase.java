package com.orionticket.ticketissuance.application.port.in;

import com.orionticket.ticketissuance.application.port.in.command.CancelTicketCommand;
import com.orionticket.ticketissuance.domain.model.Ticket;

public interface CancelTicketUseCase {
    Ticket cancelTicket(CancelTicketCommand command);
}
