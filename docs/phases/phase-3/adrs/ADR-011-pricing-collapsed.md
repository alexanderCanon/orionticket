# ADR-011: Pricing Collapsed into Orders

| Field | Value |
|---|---|
| **Date** | 2026-04-13 |
| **Status** | ACCEPTED |

## Context

The original discovery document (Block 11.1) proposed Pricing as a separate bounded context owning "Price tiers, batches, promotional codes, quota rules." During Phase 1, Batches were moved to Seating/Inventory (ADR-005). This left Pricing with only price resolution and Promotion application — both of which occur exclusively during Order creation.

## Options Considered

1. **Pricing as independent service** — Owns price tiers, Promotions, and resolution logic. Orders calls Pricing to resolve the final amount.
2. **Pricing collapsed into Orders** — Orders owns price resolution, Promotion application, and Service Fee calculation inline.

## Decision

**Pricing collapsed into Orders.** Orders owns checkout lifecycle, price resolution, and Promotion application.

## Justification

- Decided in Phase 1 (bounded-context-diagrams.md): "Pricing — Dropped → absorbed by Orders. No events of its own after Batches moved."
- After Batches moved to Seating/Inventory, Pricing had no domain events, no aggregate, and no independent lifecycle.
- Price resolution happens only at Order creation time. The Promotion is applied at the Order level (aggregate-definitions.md — Orders: promotionId, promotionDiscount).
- Maintaining a separate service for stateless computation adds latency and operational cost without value.

## Consequences

- **Positive:** One fewer service. Simpler checkout flow (no synchronous call to Pricing service). Price resolution is local to Orders context.
- **Negative:** Orders aggregate grows in responsibility. If Pricing logic becomes complex in v2 (dynamic pricing, multi-currency, loyalty tiers), extraction may be needed.
- **Mitigation:** Keep price resolution and Promotion logic as a distinct internal module within Orders, ready for extraction if needed.

## Source

- discovery.md — Block 11.1 (original Pricing definition)
- bounded-context-diagrams.md — Pricing dropped decision
- aggregate-definitions.md — Orders (promotionId, promotionDiscount, serviceFee)
