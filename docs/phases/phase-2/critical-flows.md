# Critical Flows

> **Phase:** 2 — Use Cases & Flows  
> **Source:** `docs/phases/phase-0/discovery.md` (Block 4.6, Block 5, Block 12.5), `docs/phases/phase-1/domain-events-map.md`, `docs/phases/phase-1/aggregate-definitions.md`  
> **Constraint:** Diagrams cover exactly the five flows specified in the Phase 2 prompt. Only ubiquitous language from `docs/phases/phase-1/ubiquitous-language-glossary.md` is used.

---

## Flow 1 — Organizer Creates and Submits Event for Review

Covers UC-EM-01, UC-EM-02, UC-EM-03, UC-SI-01, UC-EM-04.

```mermaid
sequenceDiagram
    actor OG as Organizer
    participant EM as Event Management
    participant SI as Seating / Inventory
    participant ID as Identity
    participant NO as Notifications
    actor PO as Platform Operator

    OG->>EM: Create Event (name, category, branding)
    EM-->>OG: EventCreated (status=DRAFT)

    OG->>EM: Add Date (datetime, Venue, Capacity)
    EM-->>OG: DateAdded

    OG->>SI: Configure Seating Map (Zones, Sections, Rows, Seats)
    SI-->>OG: SeatingMapConfigured
    Note over SI: Seats created with status=AVAILABLE

    OG->>SI: Create Batch (name, price, currency, capacity, scheduledStartAt)
    SI-->>OG: BatchCreated → BatchScheduled
    Note over SI: Batch transitions to ACTIVE at scheduledStartAt → BatchActivated

    OG->>EM: Submit Event for Review
    EM-->>OG: EventSubmittedForReview (status=UNDER_REVIEW)
    EM->>NO: Notify Platform Operator of pending review
    NO-->>PO: Notification dispatched

    PO->>EM: Approve Event
    EM-->>PO: EventReleased (status=RELEASED)
    Note over EM: Event now visible to Buyers in Catalog read model
    EM->>NO: Notify Organizer of approval
    NO-->>OG: Notification dispatched

    alt Rejected
        PO->>EM: Reject Event (with reason)
        EM-->>OG: Event status=DRAFT, rejection notes attached
        EM->>NO: Notify Organizer of rejection
        NO-->>OG: Notification dispatched
    end
```

---

## Flow 2 — Buyer Selects Seat, Reserves, Pays, Receives Ticket

Covers UC-SI-02, UC-OR-01, UC-PA-01, UC-TI-01, UC-NO-01.  
Source: discovery.md Block 4.6 checkout flow.

```mermaid
sequenceDiagram
    actor BY as Buyer
    participant EM as Event Management
    participant SI as Seating / Inventory
    participant OR as Orders
    participant PA as Payments
    participant TI as Ticket Issuance
    participant NO as Notifications

    BY->>EM: Browse Events / Select Event and Date
    EM-->>BY: Event details, Seat availability

    BY->>SI: Select Seat (MAPPED or GENERAL_ADMISSION quantity)
    Note over SI: Atomically: Seat→RESERVED, Reservation created (expiresAt=+10min), Batch.sold++
    SI-->>BY: ReservationCreated (reservationId, expiresAt)

    BY->>OR: Proceed to checkout
    OR->>OR: Resolve price (Batch price + Promotion discount + Service Fee)
    OR-->>BY: OrderCreated (orderId, total, serviceFee)

    BY->>BY: Confirm identity (already registered — required)

    BY->>PA: Submit payment (CARD or TRANSFER)
    PA->>PA: Create Payment (status=INITIATED, idempotencyKey)
    PA->>PA: Call payment gateway
    PA-->>OR: PaymentAuthorized
    OR-->>OR: Order status=CONFIRMED

    PA->>PA: Calculate Payout (groosAmount − serviceFee = netAmount)
    PA-->>PA: PayoutGenerated → PayoutProcessed (settle to Organizer)

    PA-->>TI: PaymentAuthorized (trigger issuance)
    TI->>TI: Create Ticket (status=ISSUED, dynamic QR, accessPolicy)
    TI-->>NO: TicketIssued

    NO->>BY: Deliver Ticket (EMAIL / PDF / QR / WALLET / DOWNLOAD)
    NO-->>TI: TicketDelivered
```

---

## Flow 3 — Reservation Expires Without Payment

Covers UC-SI-03.  
Source: discovery.md Block 4.4, Block 4.5.

```mermaid
sequenceDiagram
    participant JOB as Expiration Job (System)
    participant SI as Seating / Inventory
    participant OR as Orders
    participant NO as Notifications
    actor BY as Buyer

    Note over JOB: Scheduled job fires when Reservation.expiresAt ≤ now

    JOB->>SI: Detect expired Reservation (expiresAt ≤ now, status=ACTIVE)
    SI->>SI: Set Reservation.status = EXPIRED
    SI->>SI: Set Seat.status = AVAILABLE
    SI->>SI: Decrement Batch.sold
    SI-->>OR: ReservationExpired

    OR->>OR: Set Order.status = EXPIRED
    OR-->>NO: OrderExpired

    NO->>BY: Notify Buyer (Reservation expired — Seat released)
    NO-->>NO: NotificationDispatched → NotificationDelivered

    Note over SI: Seat is now available for another Buyer

    alt Payment arrives simultaneously with expiration
        BY->>PA: Submit payment for expired Order
        PA->>OR: Check Order status
        OR-->>PA: Order.status = EXPIRED
        PA-->>BY: Payment rejected (Order expired — idempotency check)
    end
```

---

## Flow 4 — Door Validator Scans QR — Success and Failure Paths

Covers UC-AC-01, UC-AC-02.  
Source: discovery.md Block 5.5, Block 8.4, Block 7.6.

```mermaid
sequenceDiagram
    actor DV as Door Validator
    participant AC as Access Control
    participant TI as Ticket Issuance
    participant NO as Notifications
    participant RP as Reporting

    DV->>AC: Scan QR code (ticketId, validatorDeviceId, eventId, dateId)
    Note over AC: Must respond within 100 ms (SLA)

    AC->>TI: Lookup Ticket by QR (read-only)
    TI-->>AC: Ticket record (status, qrExpiresAt, accessPolicy, eventId)

    alt Success path — Ticket is ISSUED, QR valid, Event matches
        AC->>AC: Create ValidationRecord (result=SUCCEEDED, isOffline=false)
        AC-->>TI: ValidationSucceeded → Set Ticket.status = USED
        AC-->>RP: TicketCheckedIn (async — eventual consistency)
        AC-->>DV: Entry GRANTED ✅
    end

    alt Failure — ALREADY_USED
        AC->>AC: Create ValidationRecord (result=FAILED, failureReason=ALREADY_USED)
        AC-->>DV: Entry DENIED ❌ (reason: already used)
        AC-->>RP: ValidationFailed (async)
    end

    alt Failure — WRONG_EVENT
        AC->>AC: Create ValidationRecord (result=FAILED, failureReason=WRONG_EVENT)
        AC-->>DV: Entry DENIED ❌ (reason: wrong event)
        AC-->>RP: ValidationFailed (async)
    end

    alt Failure — EXPIRED (QR TTL exceeded)
        AC->>AC: Create ValidationRecord (result=FAILED, failureReason=EXPIRED)
        AC-->>DV: Entry DENIED ❌ (reason: QR expired — ask Buyer to refresh)
        AC-->>RP: ValidationFailed (async)
    end

    alt Failure — INVALIDATED
        AC->>AC: Create ValidationRecord (result=FAILED, failureReason=INVALIDATED)
        AC-->>DV: Entry DENIED ❌ (reason: Ticket invalidated)
        AC-->>RP: ValidationFailed (async)
    end
```

---

## Flow 5 — Offline Validator Syncs After Reconnection

Covers UC-AC-03.  
Source: discovery.md Block 5.6, Block 12.5 flow 5.

```mermaid
sequenceDiagram
    actor DV as Door Validator (device)
    participant AC as Access Control
    participant TI as Ticket Issuance
    participant RP as Reporting

    Note over DV: Device was offline. Scans queued locally (isOffline=true).

    DV->>AC: ValidatorSyncRequested (queued ValidationRecords[])
    Note over AC: Process records in chronological order

    loop For each queued ValidationRecord
        AC->>TI: Lookup Ticket (read-only)
        TI-->>AC: Ticket status

        alt Ticket not yet USED — first to sync wins
            AC->>AC: Create ValidationRecord (result=SUCCEEDED, isOffline=true, syncedAt=now)
            AC-->>TI: Set Ticket.status = USED
            AC-->>RP: TicketCheckedIn (async)
        end

        alt Ticket already USED — conflict detected
            AC->>AC: Create ValidationRecord (result=FAILED, failureReason=ALREADY_USED, conflictDetected=true, isOffline=true, syncedAt=now)
            AC-->>AC: ConflictDetected
            Note over AC: Entry denied retroactively — first-scan-wins rule
            AC-->>RP: ConflictDetected (async)
        end
    end

    AC-->>DV: ValidatorSynced (all records reconciled)
    Note over DV: Device is now back in real-time mode

    alt Sync interrupted mid-way
        DV->>AC: ValidatorSyncRequested (remaining records)
        Note over AC: Resume from last unprocessed record
    end
```
