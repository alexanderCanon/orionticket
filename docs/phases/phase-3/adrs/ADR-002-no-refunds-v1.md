# ADR-002: No Refunds in v1

| Field | Value |
|---|---|
| **Date** | 2026-04-13 |
| **Status** | ACCEPTED |

## Context

The platform must define whether buyers can request refunds after completing a purchase. Refund support introduces significant complexity: reverse payment flows, inventory re-release, financial reconciliation adjustments, and buyer dispute handling.

## Options Considered

1. **Full refund support** — Buyer-initiated refunds with automated inventory re-release and payment reversal.
2. **Platform-initiated refunds only** — Only operators can issue refunds; buyers cannot self-serve.
3. **No refunds** — All sales are final. Platform may cancel Tickets (status = CANCELED) but no money flows back to the buyer.

## Decision

**No refunds in v1.** All sales are final. This is a firm v1 policy.

## Justification

- Explicitly decided by the client (discovery.md Block 6.5): "No refunds. All sales are final."
- Eliminates reverse payment gateway integration, reducing scope and complexity.
- Simplifies financial reconciliation — money flows one direction only.
- Refund support is listed as explicitly deferred to v2 (discovery.md Block 10.9, Block 12.4).
- Buyer disputes are mitigated by requiring explicit acceptance of terms at checkout (discovery.md Block 7.4).

## Consequences

- **Positive:** Simpler Payments and Orders aggregates. No reverse flow in Service Fee / Payout calculation. Faster v1 delivery.
- **Negative:** Buyer dissatisfaction risk. Chargeback risk if buyers dispute via their bank. Clear terms of service required.
- **Mitigation:** Explicit acceptance of no-refund policy at checkout. Chargeback reports included in v1 financial reporting (discovery.md Block 6.8).

## Source

- discovery.md — Block 3.8, Block 6.5, Block 10.9, Block 12.4, Block 12.6
