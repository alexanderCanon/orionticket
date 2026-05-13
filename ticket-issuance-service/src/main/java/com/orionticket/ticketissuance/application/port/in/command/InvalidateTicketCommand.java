package com.orionticket.ticketissuance.application.port.in.command;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record InvalidateTicketCommand(
        @NotNull UUID ticketId
) {
}
