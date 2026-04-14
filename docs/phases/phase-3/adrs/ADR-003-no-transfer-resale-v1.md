# ADR-003: No Ticket Transfer or Resale in v1

| Field | Value |
|---|---|
| **Date** | 2026-04-13 |
| **Status** | ACCEPTED |

## Context

Resale and transfer of Tickets are common features in ticketing platforms. However, they enable secondary markets that can lead to fraud, price gouging, and access control problems at the door — the exact problems OrionTicket was designed to solve.

## Options Considered

1. **Allow controlled transfers** — Buyer can transfer a Ticket to another registered user with identity re-binding.
2. **Allow marketplace resale** — Platform-mediated resale with price caps.
3. **No transfer, no resale** — A Ticket belongs to the original buyer until the Event. Once issued: USED, CANCELED, or INVALIDATED only.

## Decision

**No resale. No transfer.** A Ticket belongs to the original buyer until the event. This is a firm v1 policy.

## Justification

- Explicitly decided by the client (discovery.md Block 3.10): "No resale. No transfer."
- Eliminates an entire category of fraud: unauthorized resale, duplicate ownership, and identity mismatch at the door.
- Simplifies Ticket Issuance and Access Control — one owner, one QR, one lifecycle.
- Transfer and resale are listed as v2 candidates (discovery.md Block 10.9).

## Consequences

- **Positive:** Simpler Ticket aggregate (no ownership chain). Cleaner Access Control (holderName is immutable). Anti-fraud by design.
- **Negative:** Buyers cannot recover money if unable to attend (compounded by no refunds). Reduces flexibility for legitimate use cases.
- **Mitigation:** Clear terms at registration and checkout.

## Source

- discovery.md — Block 3.8, Block 3.10, Block 10.9
- aggregate-definitions.md — Ticket Issuance (status: ISSUED | CANCELED | INVALIDATED | USED — no TRANSFERRED state)
