package com.orionticket.ticketissuance.application.port.in;

import com.orionticket.ticketissuance.application.port.in.command.InvalidateTicketCommand;
import com.orionticket.ticketissuance.domain.model.Ticket;

public interface InvalidateTicketUseCase {
    Ticket invalidateTicket(InvalidateTicketCommand command);
}
