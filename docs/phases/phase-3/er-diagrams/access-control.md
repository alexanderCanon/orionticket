# ER Diagram — Access Control

> **Bounded Context:** Access Control  
> **Owned by:** Access Control Service  
> **Source:** `docs/phases/phase-1/aggregate-definitions.md`

```mermaid
erDiagram
    VALIDATION_RECORD {
        uuid validationId PK
        uuid ticketId "ref: TicketIssuance.ticketId (one-way)"
        string validatorDeviceId
        uuid eventId "ref: EventManagement.eventId"
        uuid dateId "ref: EventManagement.dateId"
        datetime attemptedAt
        string result "SUCCEEDED | FAILED"
        string failureReason "ALREADY_USED | WRONG_EVENT | EXPIRED | INVALIDATED | null"
        boolean isOffline "was device offline at scan time"
        datetime syncedAt "null until reconciled"
        boolean conflictDetected "true if first-scan-wins triggered"
    }
```

> **Cross-service references:** `ticketId` → Ticket Issuance (one-way, read-only — ADR-013). `eventId`, `dateId` → Event Management. All by ID only. ValidationRecord is immutable — append-only, never modified after creation.
