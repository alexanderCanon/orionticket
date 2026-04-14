# ER Diagram — Payments

> **Bounded Context:** Payments  
> **Owned by:** Payments Service  
> **Source:** `docs/phases/phase-1/aggregate-definitions.md`

```mermaid
erDiagram
    PAYMENT {
        uuid paymentId PK
        uuid orderId "ref: Orders.orderId"
        uuid buyerId "ref: Identity.userId"
        decimal amount
        decimal serviceFee
        string currency
        string method "CARD | TRANSFER"
        string status "INITIATED | AUTHORIZED | FAILED"
        string gatewayReference "external transaction ID"
        string idempotencyKey UK
        datetime createdAt
    }

    PAYOUT {
        uuid payoutId PK
        uuid organizerId "ref: Identity.userId"
        uuid eventId "ref: EventManagement.eventId"
        uuid dateId "ref: EventManagement.dateId"
        decimal grossAmount
        decimal serviceFeeTotal
        decimal netAmount
        string status "PENDING | PROCESSED | FAILED"
        integer retryCount "max 1 automatic retry"
        datetime triggeredAt
        datetime processedAt "null until processed"
    }
```

> **Cross-service references:** `orderId` → Orders. `buyerId`, `organizerId` → Identity. `eventId`, `dateId` → Event Management. All by ID only. No relationship between Payment and Payout at the DB level — Payout is generated asynchronously after the Date passes (ADR-009).
