# ADR-012: AuditLog as Cross-Cutting Concern

| Field | Value |
|---|---|
| **Date** | 2026-04-13 |
| **Status** | ACCEPTED |

## Context

A full audit trail is required for all sensitive changes (discovery.md Block 7.5). The question is whether AuditLog is owned by a specific service or treated as a cross-cutting infrastructure concern shared by all services.

## Options Considered

1. **Dedicated Audit service** — A standalone service that receives audit events and owns the AuditLog database. Other services publish audit events to it.
2. **Cross-cutting concern — written by every service** — Each service writes AuditLog entries directly to a centralized append-only store. No dedicated Audit service.
3. **Per-service local audit logs** — Each service maintains its own AuditLog table. Centralized view assembled by Reporting.

## Decision

**Cross-cutting concern — written by every service to a centralized append-only store.** AuditLog is not owned by any single service.

## Justification

- Decided in Phase 1 (aggregate-definitions.md): "AuditLog — written by every service, owned by none."
- AuditLog entries must be recorded for all sensitive operations across all bounded contexts — not just one.
- An append-only centralized store (not a traditional service with business logic) minimizes latency impact on the calling service.
- No business rules operate on AuditLog data — it is purely observational and forensic.

## Consequences

- **Positive:** Every service can emit audit entries without dependency on a separate service. Append-only guarantees immutability. Centralized store enables cross-service queries.
- **Negative:** All services need a shared AuditLog client library. The centralized store becomes a critical infrastructure dependency (must be highly available). Schema changes to AuditLog affect all services.
- **Mitigation:** AuditLog writes should be async (fire-and-forget with retry) to avoid blocking business operations. Schema is minimal and stable (actorId, actorRole, action, targetEntity, targetId, previousState, newState, occurredAt).

## Source

- discovery.md — Block 7.5
- aggregate-definitions.md — Cross-cutting (AuditLog structure)
