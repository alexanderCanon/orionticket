# ADR-004: Reservation is Universal (Mapped and General Admission)

| Field | Value |
|---|---|
| **Date** | 2026-04-13 |
| **Status** | ACCEPTED |

## Context

The platform supports two seating models: mapped venues (assigned seats) and general admission (unassigned). The domain modeling phase needed to decide whether these are separate aggregates with different lifecycles or unified under one concept.

## Options Considered

1. **Separate aggregates** — Seat for mapped venues, Slot for general admission. Different reservation mechanics, different state machines.
2. **Unified aggregate with type discriminator** — Single Seat aggregate with `type: MAPPED | GENERAL_ADMISSION`. Same Reservation child entity, same lifecycle.

## Decision

**Unified Seat aggregate with type discriminator.** Reservation works identically for both models. `type: MAPPED | GENERAL_ADMISSION` replaces the Seat vs Slot distinction — one unified concept, two behaviors.

## Justification

- Decided in Phase 1 (aggregate-definitions.md — Seating/Inventory): "type: MAPPED | GENERAL_ADMISSION replaces the Seat vs Slot distinction."
- Both types need the same concurrency guarantees: hold time (10 min), first-to-pay wins, atomic Batch.sold increment.
- A single code path reduces duplication and bug surface. The only behavioral difference is whether zone/section/row are populated (nullable for general admission).
- Simplifies Order LineItem — always references a seatId, regardless of type.

## Consequences

- **Positive:** Single Reservation flow. Single expiration job. Single Batch accounting logic. Simpler Orders integration.
- **Negative:** The Seat aggregate carries nullable fields (zone, section, row) that only apply to MAPPED. Must guard against invalid state combinations (e.g., GENERAL_ADMISSION seat with a row value).
- **Mitigation:** Validation rules enforced at aggregate creation.

## Source

- discovery.md — Block 3.3, Block 3.4
- aggregate-definitions.md — Seating/Inventory
