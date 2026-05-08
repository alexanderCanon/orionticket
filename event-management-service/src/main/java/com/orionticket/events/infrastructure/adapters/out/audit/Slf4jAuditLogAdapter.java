package com.orionticket.events.infrastructure.adapters.out.audit;

import com.orionticket.events.application.port.out.AuditLogPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class Slf4jAuditLogAdapter implements AuditLogPort {

    @Override
    public void logAction(UUID actorId, String action, String targetEntity, UUID targetId, String details) {
        log.info("AUDIT_LOG | Actor: {} | Action: {} | Entity: {} | ID: {} | Details: {}", 
                 actorId, action, targetEntity, targetId, details);
    }
}
