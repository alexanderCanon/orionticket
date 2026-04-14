# ER Diagram — Orders

> **Bounded Context:** Orders  
> **Owned by:** Orders Service  
> **Source:** `docs/phases/phase-1/aggregate-definitions.md`  
> **Note:** Pricing (price resolution, Promotions) collapsed into Orders (ADR-011).

```mermaid
erDiagram
    ORDER_TABLE {
        uuid orderId PK
        uuid buyerId "ref: Identity.userId"
        uuid eventId "ref: EventManagement.eventId"
        uuid dateId "ref: EventManagement.dateId"
        uuid reservationId "ref: SeatingInventory.reservationId"
        string status "CREATED | PAYMENT_INITIATED | CONFIRMED | EXPIRED | FAILED"
        decimal subtotal
        uuid promotionId "ref: PROMOTION.promotionId, nullable"
        decimal promotionDiscount
        decimal serviceFee
        decimal total
        string currency
        datetime createdAt
    }

    LINE_ITEM {
        uuid lineItemId PK
        uuid orderId FK
        uuid seatId "ref: SeatingInventory.seatId"
        decimal batchPrice
        integer quantity
    }

    PROMOTION {
        uuid promotionId PK
        uuid eventId "ref: EventManagement.eventId"
        string code
        string discountType "PERCENTAGE | FIXED"
        decimal discountValue
        integer maxUses
        integer usedCount
        string status "CREATED | ACTIVE | DEACTIVATED | EXHAUSTED"
    }

    ORDER_TABLE ||--o{ LINE_ITEM : "contains"
    PROMOTION ||--o{ ORDER_TABLE : "applied to"
```

> **Cross-service references:** `buyerId` → Identity. `eventId`, `dateId` → Event Management. `reservationId` → Seating/Inventory. `seatId` → Seating/Inventory. All by ID only.
