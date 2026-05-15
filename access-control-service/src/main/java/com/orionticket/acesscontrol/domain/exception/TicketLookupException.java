package com.orionticket.acesscontrol.domain.exception;

public class TicketLookupException extends RuntimeException {
    public TicketLookupException(String message) {
        super(message);
    }

    public TicketLookupException(String message, Throwable cause) {
        super(message, cause);
    }
}