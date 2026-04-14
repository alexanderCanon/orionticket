# ER Diagram — Ticket Issuance

> **Bounded Context:** Ticket Issuance  
> **Owned by:** Ticket Issuance Service  
> **Source:** `docs/phases/phase-1/aggregate-definitions.md`

```mermaid
erDiagram
    TICKET {
        uuid ticketId PK
        uuid orderId "ref: Orders.orderId"
        uuid buyerId "ref: Identity.userId"
        uuid eventId "ref: EventManagement.eventId"
        uuid dateId "ref: EventManagement.dateId"
        uuid seatId "ref: SeatingInventory.seatId, null for GA"
        string type "MAPPED | GENERAL_ADMISSION"
        string holderName
        string qrCode "dynamic, regenerates every 2 min"
        datetime qrExpiresAt "TTL control"
        string accessPolicy
        string status "ISSUED | CANCELED | INVALIDATED | USED"
        datetime deliveredAt "null until delivered"
        datetime issuedAt
    }

    TICKET_DELIVERY {
        uuid deliveryId PK
        uuid ticketId FK
        string channel "EMAIL | PDF | QR | WALLET | DOWNLOAD"
        string status "PENDING | DELIVERED | FAILED"
        datetime deliveredAt
    }

    TICKET ||--o{ TICKET_DELIVERY : "delivered via"
```

> **Cross-service references:** `orderId` → Orders. `buyerId` → Identity. `eventId`, `dateId` → Event Management. `seatId` → Seating/Inventory. All by ID only. Access Control references `ticketId` one-way (ADR-013).
