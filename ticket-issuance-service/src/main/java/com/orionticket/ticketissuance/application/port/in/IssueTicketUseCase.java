package com.orionticket.ticketissuance.application.port.in;

import com.orionticket.ticketissuance.application.port.in.command.IssueTicketCommand;
import com.orionticket.ticketissuance.domain.model.Ticket;

public interface IssueTicketUseCase {
    Ticket issueTicket(IssueTicketCommand command);
}
