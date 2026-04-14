# ADR-008: Idempotency Key Composition Strategy

| Field | Value |
|---|---|
| **Date** | 2026-04-13 |
| **Status** | ACCEPTED |

## Context

In a distributed microservices system, network failures, retries, and at-least-once message delivery can cause duplicate processing. A well-defined idempotency strategy is required to ensure that critical operations (Order creation, Payment processing, Ticket issuance) are never executed more than once for the same intent.

## Options Considered

1. **Client-generated UUID** — The client generates a unique key per request. Simple but opaque — no semantic meaning, harder to debug.
2. **Server-generated deduplication** — The server detects duplicates by comparing request bodies. Fragile, expensive, and prone to false positives.
3. **Composite key from business identifiers** — Key composed from transaction ID + buyer ID + event ID + seat ID + timestamp. Semantically meaningful, debuggable, and deterministic.

## Decision

**Composite key from business identifiers:** `transactionId + buyerId + eventId + seatId + timestamp (millisecond precision)`. Applied at Orders, Payments, and Ticket Issuance boundaries.

## Justification

- Decided in Phase 0 (discovery.md Block 11.4): "Idempotency keys composed from: transaction ID + buyer ID + event ID + seat ID + timestamp (to millisecond precision). Applied at Orders, Payments, and Issuance boundaries."
- Composite keys are deterministic — the same intent always produces the same key, regardless of retries.
- Semantically meaningful — a key can be decoded for debugging and auditing.
- Applied at the three critical boundary surfaces where duplicate processing has zero-tolerance consequences (double charge, double issuance, double order).

## Consequences

- **Positive:** Deterministic deduplication. Debuggable keys. Consistent strategy across three services. Prevents double charge, double issuance, double order.
- **Negative:** Slightly longer key values (composite vs UUID). All components of the key must be available at the time of key generation — timestamp precision must be consistent across services.
- **Mitigation:** Use UTC timestamps with millisecond precision. Key generation as a shared library or standardized utility across Orders, Payments, Ticket Issuance.

## Source

- discovery.md — Block 11.4
