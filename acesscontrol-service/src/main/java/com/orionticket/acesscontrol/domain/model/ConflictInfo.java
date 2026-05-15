package com.orionticket.acesscontrol.domain.model;

import java.util.UUID;

public record ConflictInfo(
        UUID conflictWithValidationId,
        UUID ticketId
) {
    public static ConflictInfo of(UUID conflictWithValidationId, UUID ticketId) {
        return new ConflictInfo(conflictWithValidationId, ticketId);
    }
}