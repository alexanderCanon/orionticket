# ADR-009: Payout Triggered Automatically After Date Passes

| Field | Value |
|---|---|
| **Date** | 2026-04-13 |
| **Status** | ACCEPTED |

## Context

The platform retains a Service Fee per transaction and settles the remainder to the Organizer as a Payout. The question is: when is the Payout triggered? Options include immediately after each payment, in batches nightly, or after the Event Date passes to guarantee fulfillment.

## Options Considered

1. **Immediate payout per payment** — Payout triggered on each PaymentAuthorized. Fast for Organizers but risky — event could be canceled after payout.
2. **Nightly batch payout** — Aggregate all confirmed payments into a daily settlement. Reduces gateway calls but doesn't protect against event cancellation.
3. **Post-Date payout** — Payout triggered automatically after the Event Date passes. Ensures the event actually happened before money is settled.

## Decision

**Payout triggered automatically after the Event Date passes.** The system aggregates all confirmed Payments for a given organizerId + eventId + dateId and generates a Payout after the Date's scheduled time has elapsed.

## Justification

- The Payout aggregate references organizerId, eventId, and dateId — indicating settlement is scoped per Date, not per individual payment (aggregate-definitions.md — Payments).
- Settling after the Date passes protects the platform against cancellation risk: if an Event is canceled before it occurs, no Payout has been made.
- discovery.md Block 6.3 states "The remainder is settled to the organizer" and Block 6.5 confirms no refunds — but cancellation by the platform/organizer is possible (Ticket status = CANCELED). Post-Date payout avoids the need to claw back settlements.
- Financial reconciliation is required from v1 (discovery.md Block 6.4). Post-Date settlement simplifies reconciliation — one Payout per Date.

## Consequences

- **Positive:** Platform never overpays. Cancellation handling is simpler (no Payout to reverse). One reconciliation entry per Date per Organizer.
- **Negative:** Organizer wait time — they don't receive funds until after the event. May cause cash-flow friction for Organizers.
- **Mitigation:** Clear Payout timeline communicated to Organizers at onboarding. Payout has max 1 automatic retry on failure (aggregate-definitions.md — Payout.retryCount).

## Source

- aggregate-definitions.md — Payments (Payout: organizerId, eventId, dateId, triggeredAt)
- discovery.md — Block 6.3, Block 6.4, Block 6.5
- domain-events-map.md — Financial (PayoutGenerated, PayoutProcessed, PayoutFailed)
