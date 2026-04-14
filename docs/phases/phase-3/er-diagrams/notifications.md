# ER Diagram — Notifications

> **Bounded Context:** Notifications  
> **Owned by:** Notifications Service  
> **Source:** `docs/phases/phase-1/aggregate-definitions.md`

```mermaid
erDiagram
    NOTIFICATION {
        uuid notificationId PK
        uuid recipientId "ref: Identity.userId"
        string channel "EMAIL | SMS | WHATSAPP"
        string templateId "message template reference"
        json payload "dynamic data for template"
        string status "PENDING | DISPATCHED | DELIVERED | FAILED"
        integer retryCount "Support triggers manual resend"
        string triggeredBy "domain event that caused this"
        datetime createdAt
    }
```

> **Cross-service references:** `recipientId` → Identity. All by ID only. Notifications is a terminal service — no downstream consumers depend on its data.
