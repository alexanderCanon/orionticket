package com.orionticket.acesscontrol.domain.exception;

public class TicketNotFoundException extends RuntimeException {
    private final String ticketId;

    public TicketNotFoundException(String ticketId) {
        super("Ticket not found: " + ticketId);
        this.ticketId = ticketId;
    }

    public String getTicketId() {
        return ticketId;
    }
}