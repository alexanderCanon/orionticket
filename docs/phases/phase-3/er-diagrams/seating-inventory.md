# ER Diagram — Seating / Inventory

> **Bounded Context:** Seating / Inventory  
> **Owned by:** Seating/Inventory Service  
> **Source:** `docs/phases/phase-1/aggregate-definitions.md`  
> **Note:** Batch is a child entity of the Seat aggregate (ADR-005). Reservation is a child entity of Seat.

```mermaid
erDiagram
    SEAT {
        uuid seatId PK
        uuid eventId "ref: EventManagement.eventId"
        uuid dateId "ref: EventManagement.dateId"
        string zone "nullable for GENERAL_ADMISSION"
        string section "nullable for GENERAL_ADMISSION"
        string row "nullable for GENERAL_ADMISSION"
        string type "MAPPED | GENERAL_ADMISSION"
        string status "AVAILABLE | RESERVED | SOLD | BLOCKED"
        string accessPolicy
        uuid batchId FK
    }

    RESERVATION {
        uuid reservationId PK
        uuid seatId FK
        uuid buyerId "ref: Identity.userId"
        datetime expiresAt
        string status "ACTIVE | EXPIRED | RELEASED"
    }

    BATCH {
        uuid batchId PK
        uuid eventId "ref: EventManagement.eventId"
        uuid dateId "ref: EventManagement.dateId"
        string name
        decimal price
        string currency
        integer capacity
        integer sold "incremented atomically with Reservation"
        string status "SCHEDULED | ACTIVE | EXHAUSTED | EXPIRED"
        datetime scheduledStartAt
    }

    SEAT ||--o| RESERVATION : "holds"
    BATCH ||--o{ SEAT : "prices"
```

> **Cross-service references:** `eventId`, `dateId` reference Event Management by ID. `buyerId` references Identity by ID. No foreign keys across service boundaries.
