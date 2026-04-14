# ADR-005: Atomic Batch and Reservation Consistency

| Field | Value |
|---|---|
| **Date** | 2026-04-13 |
| **Status** | ACCEPTED |

## Context

When a Buyer selects a Seat, two things must happen atomically: the Reservation is created (Seat locked for 10 min) and the Batch.sold counter is incremented. If these are not atomic, the system risks overselling a Batch quota or holding a Seat without decrementing Batch capacity.

## Options Considered

1. **Eventual consistency** — Create Reservation first, then async increment Batch.sold. Roll back Reservation if Batch is full.
2. **Atomic within aggregate** — Batch is a child entity of the Seat aggregate. Reservation creation and Batch.sold increment happen in the same transaction.
3. **Saga pattern** — Distributed transaction across separate Seat and Batch aggregates with compensating actions.

## Decision

**Atomic within aggregate.** Batch is a child entity of the Seat aggregate root. Reservation creation and Batch.sold increment happen in the same write operation.

## Justification

- Decided in Phase 1 (aggregate-definitions.md — Seating/Inventory): Batch is listed as a child entity of the Seat aggregate root, and "sold ← incremented atomically with Reservation."
- Seat overbooking is a zero-tolerance incident (discovery.md Block 7.6). Eventual consistency between Batch and Reservation would introduce a window for overselling.
- Keeping Batch inside the Seat aggregate boundary means a single database transaction — no distributed coordination needed.
- During Phase 1, Batches were moved from the Pricing context to Seating/Inventory specifically for this reason (bounded-context-diagrams.md: "Batches moved from Pricing → Seating/Inventory — Inventory concept, not a price concept").

## Consequences

- **Positive:** Zero overselling risk. Single transactional boundary. No saga complexity. Simple rollback on failure.
- **Negative:** Larger aggregate boundary — more fields per Seat record. Higher contention per Seat row under peak load. Must use row-level or pessimistic locking.
- **Mitigation:** Seat-level locking (pessimistic or distributed lock) designed before code is written. Load testing at 30K concurrent users before launch.

## Source

- aggregate-definitions.md — Seating/Inventory (Batch as child entity, sold atomic increment)
- bounded-context-diagrams.md — Batches moved to Seating/Inventory
- discovery.md — Block 7.6, Block 11.7
