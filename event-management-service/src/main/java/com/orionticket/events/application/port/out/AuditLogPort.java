package com.orionticket.events.application.port.out;

import java.util.UUID;

/**
 * Puerto para registrar logs de auditoría de cambios sensibles.
 * Cumple con BR-CC-01.
 */
public interface AuditLogPort {
    void logAction(UUID actorId, String action, String targetEntity, UUID targetId, String details);
}
