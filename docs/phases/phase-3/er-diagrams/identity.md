# ER Diagram — Identity

> **Bounded Context:** Identity  
> **Owned by:** Identity Service  
> **Source:** `docs/phases/phase-1/aggregate-definitions.md`

```mermaid
erDiagram
    USER {
        uuid userId PK
        string email UK
        string passwordHash
        string fullName
        string phone
        string status "ACTIVE | SUSPENDED | UNVERIFIED"
        uuid roleId FK
        uuid organizerId "null for platform-level users"
        datetime createdAt
    }

    ROLE {
        uuid roleId PK
        string name UK
    }

    PERMISSION {
        uuid permissionId PK
        uuid roleId FK
        string permission
    }

    ROLE ||--o{ PERMISSION : "has"
    ROLE ||--o{ USER : "assigned to"
```

> **Cross-service references:** `organizerId` references an Organizer managed within Identity (organizerId = userId of an Organizer-role User). Other services reference `userId` by ID only — no foreign keys across boundaries.
