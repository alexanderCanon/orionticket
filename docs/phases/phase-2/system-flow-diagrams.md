# System Flow Diagrams

> **Phase:** 2 — Use Cases & Flows  
> **Purpose:** Complement `critical-flows.md` with end-to-end system diagrams that include frontend entry points, login, API Gateway routing, actors, and service collaboration.  
> **Security note:** These diagrams treat the access token as the logical authentication contract. The implementation detail for token validation (shared JWT secret vs. asymmetric JWKS/OAuth2 Resource Server) is pending a security decision.

---

## Actors and Entry Points

```mermaid
flowchart LR
    BY[Buyer] --> BP[Buyer Portal]
    OG[Organizer] --> OP[Organizer Panel]
    DV[Door Validator] --> VA[Validator App]
    VS[Venue Staff] --> OP
    SU[Support] --> BO[Backoffice]
    FI[Finance] --> BO
    MK[Marketing] --> BO
    PO[Platform Operator] --> BO
    SA[Super Admin] --> BO

    BP --> GW[API Gateway]
    OP --> GW
    VA --> GW
    BO --> GW

    GW --> ID[Identity]
    GW --> EM[Event Management]
    GW --> SI[Seating / Inventory]
    GW --> OR[Orders]
    GW --> PA[Payments]
    GW --> TI[Ticket Issuance]
    GW --> AC[Access Control]
    GW --> NO[Notifications]
    GW --> RE[Reporting]

    ID --> IDDB[(Identity DB)]
    EM --> EMDB[(Event Management DB)]
    SI --> SIDB[(Seating / Inventory DB)]
    OR --> ORDB[(Orders DB)]
    PA --> PADB[(Payments DB)]
    TI --> TIDB[(Ticket Issuance DB)]
    AC --> ACDB[(Access Control DB)]
    NO --> NODB[(Notifications DB)]
    RE --> REDB[(Reporting DB)]

    EM -. domain events .-> MQ[(RabbitMQ)]
    SI -. domain events .-> MQ
    OR -. domain events .-> MQ
    PA -. domain events .-> MQ
    TI -. domain events .-> MQ
    AC -. domain events .-> MQ
    NO -. notification events .-> MQ

    MQ -. projections / reactions .-> SI
    MQ -. projections / reactions .-> PA
    MQ -. projections / reactions .-> TI
    MQ -. delivery .-> NO
    MQ -. read models .-> RE
```

---

## Login and Authenticated Request

This diagram describes the required logical flow for all web applications. It does not decide whether JWT validation is implemented with a shared secret or JWKS.

```mermaid
sequenceDiagram
    actor U as User
    participant WEB as Web App
    participant GW as API Gateway
    participant ID as Identity
    participant SVC as Protected Service

    U->>WEB: Enter email and password
    WEB->>GW: POST /v1/auth/login
    GW->>ID: Route login request
    ID->>ID: Validate credentials with BCrypt hash
    ID-->>GW: accessToken, userId, roleId, organizerId
    GW-->>WEB: Auth response

    WEB->>WEB: Store access token for session

    U->>WEB: Open protected screen
    WEB->>GW: API request with Authorization: Bearer token
    GW->>GW: Validate token and route request
    GW->>SVC: Forward authenticated request with user context
    SVC->>SVC: Enforce role and ownership rules
    SVC-->>GW: Response
    GW-->>WEB: Response

    alt Token missing, invalid, or expired
        GW-->>WEB: 401 Unauthorized
        WEB-->>U: Redirect to login
    end

    alt Authenticated but forbidden
        SVC-->>GW: 403 Forbidden
        GW-->>WEB: 403 Forbidden
    end
```

---

## Buyer Purchase Journey

```mermaid
sequenceDiagram
    actor BY as Buyer
    participant BP as Buyer Portal
    participant GW as API Gateway
    participant ID as Identity
    participant EM as Event Management
    participant SI as Seating / Inventory
    participant OR as Orders
    participant PA as Payments
    participant TI as Ticket Issuance
    participant NO as Notifications
    participant MQ as RabbitMQ

    BY->>BP: Register or login
    BP->>GW: POST /v1/auth/register or /v1/auth/login
    GW->>ID: Route auth request
    ID-->>BP: accessToken and user context

    BY->>BP: Browse released events
    BP->>GW: GET /v1/catalog/events
    GW->>EM: Route catalog request
    EM-->>BP: Released events and dates

    BY->>BP: Select event date and seats
    BP->>GW: GET availability / create reservation
    GW->>SI: Route seat and reservation request
    SI->>SI: Hold seat atomically and create Reservation
    SI-->>MQ: ReservationCreated
    SI-->>BP: reservationId and expiresAt

    BY->>BP: Proceed to checkout
    BP->>GW: POST /v1/orders
    GW->>OR: Route order request
    OR->>OR: Resolve price, promotion, service fee
    OR-->>MQ: OrderCreated
    OR-->>BP: orderId and total

    BY->>BP: Submit payment
    BP->>GW: POST /v1/payments
    GW->>PA: Route payment request
    PA->>PA: Create Payment and call gateway
    PA-->>MQ: PaymentAuthorized

    MQ-->>OR: PaymentAuthorized
    OR->>OR: Confirm Order

    MQ-->>TI: PaymentAuthorized
    TI->>TI: Issue Ticket with dynamic QR
    TI-->>MQ: TicketIssued

    MQ-->>NO: TicketIssued
    NO->>BY: Deliver ticket by configured channel
```

---

## Organizer Event Publication Journey

```mermaid
sequenceDiagram
    actor OG as Organizer
    actor PO as Platform Operator
    participant OP as Organizer Panel
    participant BO as Backoffice
    participant GW as API Gateway
    participant ID as Identity
    participant EM as Event Management
    participant SI as Seating / Inventory
    participant NO as Notifications
    participant MQ as RabbitMQ

    OG->>OP: Login
    OP->>GW: POST /v1/auth/login
    GW->>ID: Route login request
    ID-->>OP: accessToken and organizerId

    OG->>OP: Create venue, event, and dates
    OP->>GW: POST /v1/venues, POST /v1/events, POST /v1/events/{eventId}/dates
    GW->>EM: Route management requests
    EM-->>MQ: EventCreated, DateAdded
    EM-->>OP: Draft event data

    OG->>OP: Configure seating map and batches
    OP->>GW: Configure seats and batches
    GW->>SI: Route inventory configuration
    SI-->>MQ: SeatingMapConfigured, BatchCreated, BatchScheduled
    SI-->>OP: Configuration accepted

    OG->>OP: Submit event for review
    OP->>GW: POST /v1/events/{eventId}/submit
    GW->>EM: Route submission
    EM->>EM: Set Event status to UNDER_REVIEW
    EM-->>MQ: EventSubmittedForReview
    MQ-->>NO: EventSubmittedForReview
    NO->>PO: Notify pending review

    PO->>BO: Approve or reject event
    BO->>GW: POST approve/reject
    GW->>EM: Route review decision

    alt Approved
        EM->>EM: Set Event status to RELEASED
        EM-->>MQ: EventReleased
        MQ-->>NO: EventReleased
        NO->>OG: Notify approval
    else Rejected
        EM->>EM: Set Event status to DRAFT with reason
        EM-->>MQ: EventRejected
        MQ-->>NO: EventRejected
        NO->>OG: Notify rejection
    end
```

---

## Backoffice Journeys

```mermaid
flowchart TD
    BO[Backoffice Login] --> AUTH[Identity authenticates user]
    AUTH --> ROLE{Role}

    ROLE --> SA[Super Admin]
    ROLE --> PO[Platform Operator]
    ROLE --> SU[Support]
    ROLE --> FI[Finance]
    ROLE --> MK[Marketing]

    SA --> IDADM[Manage users, roles, and permissions]
    SA --> ALL[Access all platform modules]

    PO --> REVIEW[Approve or reject submitted events]
    PO --> OVERRIDE[Override reservations during incidents]
    PO --> REPORTS[View platform reports]

    SU --> BUYER[View buyer and order context]
    SU --> TICKET[View ticket details]
    SU --> RESEND[Trigger manual ticket resend]

    FI --> PAY[View payments and payouts]
    FI --> FINREP[Export settlement reports]

    MK --> ANALYTICS[View buyer and event analytics]
    MK --> PROMO[Manage promotions]

    IDADM --> GW[API Gateway]
    ALL --> GW
    REVIEW --> GW
    OVERRIDE --> GW
    REPORTS --> GW
    BUYER --> GW
    TICKET --> GW
    RESEND --> GW
    PAY --> GW
    FINREP --> GW
    ANALYTICS --> GW
    PROMO --> GW
```

---

## Door Validation Journey

```mermaid
sequenceDiagram
    actor DV as Door Validator
    participant VA as Validator App
    participant GW as API Gateway
    participant ID as Identity
    participant AC as Access Control
    participant TI as Ticket Issuance
    participant RE as Reporting
    participant MQ as RabbitMQ

    DV->>VA: Login
    VA->>GW: POST /v1/auth/login
    GW->>ID: Route login request
    ID-->>VA: accessToken with Door Validator role

    DV->>VA: Scan QR
    VA->>GW: POST /v1/validations
    GW->>AC: Route validation request
    AC->>TI: Lookup Ticket by QR
    TI-->>AC: Ticket status, eventId, dateId, qrExpiresAt

    alt Valid ticket
        AC->>AC: Create ValidationRecord SUCCEEDED
        AC-->>MQ: ValidationSucceeded
        MQ-->>TI: ValidationSucceeded
        TI->>TI: Set Ticket status to USED
        MQ-->>RE: TicketCheckedIn projection
        AC-->>VA: Entry granted
    else Invalid ticket
        AC->>AC: Create ValidationRecord FAILED
        AC-->>MQ: ValidationFailed
        MQ-->>RE: ValidationFailed projection
        AC-->>VA: Entry denied with reason
    end
```

---

## Security Responsibility Boundary

```mermaid
flowchart LR
    WEB[Web Apps] -->|credentials| ID[Identity]
    ID -->|issues access token| WEB
    WEB -->|Bearer token| GW[API Gateway]

    GW -->|validate token decision pending| AUTH{Token valid?}
    AUTH -->|No| U401[401 Unauthorized]
    AUTH -->|Yes| ROUTE[Route to service]

    ROUTE --> SVC[Microservice]
    SVC -->|role + ownership checks| AUTHZ{Allowed?}
    AUTHZ -->|No| U403[403 Forbidden]
    AUTHZ -->|Yes| OK[Business operation]

    SVC -. uses IDs only .-> OWN[Own database]
    SVC -. emits/consumes events .-> MQ[(RabbitMQ)]
```

### Pending Security Decision

The diagrams above require one concrete implementation decision before production hardening:

- Shared-secret JWT validation across Gateway and services.
- Asymmetric JWT validation through JWKS / OAuth2 Resource Server.

The API Gateway ADR requires JWT validation at the gateway without per-request calls to Identity. Microservices still need enough authenticated user context to enforce role and ownership rules.
