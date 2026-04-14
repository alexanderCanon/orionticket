# ER Diagram — Reporting

> **Bounded Context:** Reporting  
> **Owned by:** Reporting Service  
> **Source:** `docs/phases/phase-1/aggregate-definitions.md`  
> **Note:** Reporting has no traditional aggregates — only read models (projections built from domain events).

```mermaid
erDiagram
    SALES_REPORT {
        uuid reportId PK
        uuid organizerId "ref: Identity.userId"
        uuid eventId "ref: EventManagement.eventId"
        uuid dateId "ref: EventManagement.dateId"
        integer totalTicketsSold
        decimal totalRevenue
        decimal totalServiceFees
        decimal totalPayouts
        datetime generatedAt
    }

    COMMISSION_REPORT {
        uuid reportId PK
        uuid organizerId "ref: Identity.userId"
        datetime periodStart
        datetime periodEnd
        decimal totalServiceFees
        datetime generatedAt
    }

    ACCESS_REPORT {
        uuid reportId PK
        uuid eventId "ref: EventManagement.eventId"
        uuid dateId "ref: EventManagement.dateId"
        integer totalValidations
        integer succeeded
        integer failed
        integer offlineScans
        integer conflictsDetected
        datetime generatedAt
    }
```

> **Cross-service references:** `organizerId` → Identity. `eventId`, `dateId` → Event Management. All by ID only. Reports are eventually consistent projections — populated by consuming domain events (TicketIssued, PaymentAuthorized, PayoutProcessed, ValidationSucceeded, etc.). Reporting never queries other services' databases directly (discovery.md Block 11.2).
