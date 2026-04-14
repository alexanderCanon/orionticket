# ADR-013: One-Way Relationship Between ValidationRecord and Ticket

| Field | Value |
|---|---|
| **Date** | 2026-04-13 |
| **Status** | ACCEPTED |

## Context

Access Control creates ValidationRecords when Tickets are scanned at the door. The question is the relationship direction: does ValidationRecord reference Ticket (one-way), or does Ticket also track its ValidationRecords (bidirectional)?

## Options Considered

1. **Bidirectional** — Ticket holds a list of ValidationRecords; ValidationRecord references Ticket. Both aggregates know about each other.
2. **One-way from ValidationRecord to Ticket** — ValidationRecord holds a ticketId reference. Ticket has no knowledge of ValidationRecords. Access Control reads Ticket data via synchronous API call.
3. **No direct reference** — Both operate on shared event data only. No ID references.

## Decision

**One-way from ValidationRecord to Ticket.** ValidationRecord holds a ticketId reference (read-only). Ticket Issuance has no knowledge of Access Control or ValidationRecords.

## Justification

- Decided in Phase 1 (aggregate-definitions.md — Access Control): "ValidationRecord — immutable, one-way reference to Ticket."
- Access Control only reads Ticket data (status, QR, accessPolicy) to determine if entry is granted. It never modifies the Ticket aggregate directly — it fires TicketInvalidated as a domain event when fraud is detected, and Ticket Issuance handles the state change.
- This preserves bounded context isolation: Ticket Issuance doesn't need to know how or when validation happens.
- The Ticket aggregate stays clean — no unbounded list of ValidationRecords growing inside it.

## Consequences

- **Positive:** Clean aggregate boundaries. Ticket Issuance evolves independently. ValidationRecord is immutable (append-only), never modified. Bounded context coupling is minimal (one synchronous read call + one domain event).
- **Negative:** To check if a Ticket has been validated (USED), Access Control must query its own ValidationRecord store or Ticket Issuance must react to the ValidationSucceeded event to set Ticket status = USED.
- **Mitigation:** Ticket status = USED is set by Ticket Issuance upon receiving ValidationSucceeded event. Access Control checks Ticket status via synchronous API (100 ms SLA) before creating ValidationRecord.

## Source

- aggregate-definitions.md — Access Control (immutable, one-way reference)
- aggregate-definitions.md — Ticket Issuance (no reference to ValidationRecord)
- domain-events-map.md — TicketCheckedIn, TicketInvalidated
