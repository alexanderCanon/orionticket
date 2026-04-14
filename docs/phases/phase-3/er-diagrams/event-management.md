# ER Diagram — Event Management

> **Bounded Context:** Event Management  
> **Owned by:** Event Management Service  
> **Source:** `docs/phases/phase-1/aggregate-definitions.md`  
> **Note:** Catalog is a read model inside this context (ADR-010).

```mermaid
erDiagram
    EVENT {
        uuid eventId PK
        uuid organizerId "ref: Identity.userId"
        string name
        string description
        string category
        string status "DRAFT | UNDER_REVIEW | RELEASED | CANCELED"
        string rejectionReason "null unless rejected"
        datetime createdAt
        datetime updatedAt
    }

    DATE {
        uuid dateId PK
        uuid eventId FK
        datetime scheduledAt
        uuid venueId FK
        integer capacity
        string status "ACTIVE | CANCELED"
    }

    VENUE {
        uuid venueId PK
        uuid organizerId "ref: Identity.userId"
        string name
        string address
        integer totalCapacity
    }

    EVENT ||--o{ DATE : "has"
    VENUE ||--o{ DATE : "hosts"
```

> **Cross-service references:** `organizerId` references `Identity.userId` by ID only. Seating/Inventory uses `eventId` and `dateId` by ID only.
