package com.orionticket.acesscontrol.application.port.in.command;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ValidateTicketCommand(
        @NotNull UUID ticketId,
        @NotNull String validatorDeviceId,
        @NotNull UUID eventId,
        @NotNull UUID dateId
) {}