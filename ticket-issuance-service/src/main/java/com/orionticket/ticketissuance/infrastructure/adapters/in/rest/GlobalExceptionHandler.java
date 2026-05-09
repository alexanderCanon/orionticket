package com.orionticket.ticketissuance.infrastructure.adapters.in.rest;

import com.orionticket.ticketissuance.domain.exception.TicketNotFoundException;
import com.orionticket.ticketissuance.infrastructure.adapters.in.rest.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TicketNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleTicketNotFound(TicketNotFoundException exception, HttpServletRequest request) {
        return new ErrorResponse(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                exception.getMessage(),
                "TICKET_NOT_FOUND",
                request.getRequestURI(),
                MDC.get("traceId")
        );
    }
}
