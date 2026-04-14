# ADR-010: Catalog Collapsed into Event Management

| Field | Value |
|---|---|
| **Date** | 2026-04-13 |
| **Status** | ACCEPTED |

## Context

The original discovery document (Block 11.1) proposed Catalog as a separate bounded context that owns "Event definitions, venue metadata, public-facing listings." During Phase 1 domain modeling, the team evaluated whether Catalog has sufficient justification as an independent service.

## Options Considered

1. **Catalog as independent service** — Owns its own data, serves the public-facing event listings, receives events from Event Management.
2. **Catalog collapsed into Event Management as a read model** — Event Management owns all event data and exposes a read-optimized Catalog view. No separate service.

## Decision

**Catalog collapsed into Event Management as a read model.** Event Management owns all event data and serves the public-facing event listings directly.

## Justification

- Decided in Phase 1 (bounded-context-diagrams.md): "Catalog — Dropped → read model inside Event Management. No own data, no own rules."
- Catalog has no domain events of its own — it only reads data produced by Event Management.
- Catalog has no business rules — it's a projection of Event + Date data for public consumption.
- Maintaining a separate service with its own database for a pure read model adds operational cost without business benefit.

## Consequences

- **Positive:** One fewer service to deploy, monitor, and maintain. Simpler system. No event synchronization needed between Event Management and Catalog.
- **Negative:** Event Management service has a larger API surface (both admin and public-facing). Must ensure the read model path is optimized for public traffic and doesn't compete with admin write operations.
- **Mitigation:** Separate read-optimized database view or CQRS projection within Event Management for public catalog queries.

## Source

- discovery.md — Block 11.1 (original Catalog definition)
- bounded-context-diagrams.md — Catalog dropped decision
