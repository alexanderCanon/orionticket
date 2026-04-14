# ADR-006: Dynamic QR with 2-Minute TTL

| Field | Value |
|---|---|
| **Date** | 2026-04-13 |
| **Status** | ACCEPTED |

## Context

Ticket fraud via screenshot sharing is a common attack vector. If QR codes are static (generated once, never change), a buyer can share the image with multiple people, and the first to scan gains entry while fraud is only detected after the fact.

## Options Considered

1. **Static QR** — Generated once at issuance, never changes. Simple but vulnerable to screenshot sharing.
2. **Dynamic QR with TTL** — QR code regenerates periodically. Only the current code is valid for Validation. Prevents screenshot replay.
3. **QR + biometric / ID verification** — Static QR paired with identity check at the door. Secure but slow.

## Decision

**Dynamic QR with 2-minute TTL.** The QR code regenerates every 2 minutes. `qrExpiresAt` controls the TTL. An expired QR returns `failureReason: EXPIRED` at the door — the Buyer must refresh the code.

## Justification

- Decided in Phase 1 (aggregate-definitions.md — Ticket Issuance): "qrCode ← dynamic, regenerates every 2 minutes" and "qrExpiresAt ← TTL control."
- Eliminates screenshot sharing as a viable attack — a shared screenshot expires within 2 minutes.
- 2-minute window balances security (short enough to prevent sharing) with UX (long enough that a buyer in a queue isn't constantly refreshing).
- Validator App must be online or have recent sync to validate current QR.

## Consequences

- **Positive:** Anti-fraud by design. No screenshot replay. Each scan verifies recency.
- **Negative:** Requires Buyer device to be online to regenerate QR. Adds complexity to Ticket Issuance (periodic regeneration or on-demand generation). Offline validators may hold stale QR references — mitigated by sync protocol (ADR-007).
- **Mitigation:** QR refresh can be triggered on-demand when Buyer opens the Ticket in their portal/app. Offline validators work with last-synced QR data and resolve conflicts on reconnection.

## Source

- aggregate-definitions.md — Ticket Issuance (qrCode, qrExpiresAt)
- discovery.md — Block 7.6 (zero-tolerance: no duplicate QR validation)
