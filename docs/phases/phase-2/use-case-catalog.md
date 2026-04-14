# Use Case Catalog

> **Phase:** 2 — Use Cases & Flows  
> **Source:** `docs/phases/phase-0/discovery.md`, `docs/phases/phase-1/aggregate-definitions.md`, `docs/phases/phase-1/domain-events-map.md`, `docs/phases/phase-1/bounded-context-diagrams.md`, `docs/phases/phase-1/ubiquitous-language-glossary.md`  
> **Constraint:** One use case per meaningful actor–aggregate interaction. Only actors and concepts defined in Phase 0/1 are included.

---

## Identity

### UC-ID-01 — Buyer Self-Registration

| Field | Value |
|---|---|
| **Actor** | Buyer |
| **Aggregate** | User |
| **Preconditions** | Buyer does not have an existing account. |
| **Main flow** | 1. Buyer submits registration form (email, password, full name, phone). 2. System creates User with status UNVERIFIED. 3. System dispatches verification Notification. 4. Buyer confirms email/phone. 5. System sets User status to ACTIVE. |
| **Alternative flows** | A1 — Email already registered: system rejects with conflict error. A2 — Verification not completed: User remains UNVERIFIED and cannot purchase. |
| **Postconditions** | User exists with status ACTIVE and roleId = Buyer. |
| **Domain events fired** | *(Identity-internal; not in domain-events-map.md)* |

---

### UC-ID-02 — Organizer Staff Management

| Field | Value |
|---|---|
| **Actor** | Organizer |
| **Aggregate** | User |
| **Preconditions** | Organizer User is ACTIVE. |
| **Main flow** | 1. Organizer creates a new User scoped to their Organizer with Role = Venue Staff or Door Validator. 2. System creates User with the given Role and organizerId reference. 3. New User receives credential Notification. |
| **Alternative flows** | A1 — Organizer attempts to assign a platform-level Role (Support, Finance, etc.): system rejects — Organizers may only manage scoped Roles. |
| **Postconditions** | New User exists, scoped to Organizer, with correct Role. |
| **Domain events fired** | *(Identity-internal)* |

---

### UC-ID-03 — Super Admin User and Role Management

| Field | Value |
|---|---|
| **Actor** | Super Admin |
| **Aggregate** | User, Role |
| **Preconditions** | Super Admin User is ACTIVE. |
| **Main flow** | 1. Super Admin creates, updates, or suspends any User across any Organizer. 2. Super Admin assigns or modifies Roles and their permissions[]. 3. System persists changes. 4. AuditLog entry appended. |
| **Alternative flows** | A1 — Attempt to delete a User with active Orders: system blocks deletion. |
| **Postconditions** | User and Role data updated. AuditLog entry written. |
| **Domain events fired** | *(Identity-internal)* |

---

## Event Management

### UC-EM-01 — Organizer Creates Event with Dates

| Field | Value |
|---|---|
| **Actor** | Organizer |
| **Aggregate** | Event, Date |
| **Preconditions** | Organizer User is ACTIVE. Venue and Seating Map exist. |
| **Main flow** | 1. Organizer creates an Event (name, description, category, Organizer branding). 2. Organizer adds one or more Dates (date/time, Venue, Capacity). 3. System persists Event with status DRAFT and each Date as child entity. |
| **Alternative flows** | A1 — Required Event fields missing: system rejects with validation error. |
| **Postconditions** | Event exists in status DRAFT with one or more Dates. |
| **Domain events fired** | `EventCreated`, `DateAdded` |

---

### UC-EM-02 — Organizer Configures Seating Map

| Field | Value |
|---|---|
| **Actor** | Organizer |
| **Aggregate** | Seat (via Seating/Inventory) |
| **Preconditions** | Event and Date exist. Venue created. |
| **Main flow** | 1. Organizer defines Zones, Sections, Rows, and numbered Seats (or GENERAL_ADMISSION blocks). 2. System persists Seat records with status AVAILABLE and accessPolicy. |
| **Alternative flows** | A1 — Seating Map already active (Event Released): system blocks modification. |
| **Postconditions** | Seat records created with AVAILABLE status for the given Date. |
| **Domain events fired** | `SeatingMapConfigured` |

---

### UC-EM-03 — Organizer Submits Event for Review

| Field | Value |
|---|---|
| **Actor** | Organizer |
| **Aggregate** | Event |
| **Preconditions** | Event is in DRAFT status. At least one Date and Seating Map configured. At least one Batch is defined. |
| **Main flow** | 1. Organizer submits Event for review. 2. System sets Event status to UNDER_REVIEW. 3. Platform Operator receives notification of pending review. |
| **Alternative flows** | A1 — Event missing required data (no Dates, no Batch): system rejects submission. |
| **Postconditions** | Event status = UNDER_REVIEW. Platform Operator notified. |
| **Domain events fired** | `EventSubmittedForReview` |

---

### UC-EM-04 — Platform Operator Approves or Rejects Event

| Field | Value |
|---|---|
| **Actor** | Platform Operator |
| **Aggregate** | Event |
| **Preconditions** | Event is in UNDER_REVIEW status. |
| **Main flow** | 1. Platform Operator reviews Event details. 2a. If approved: system sets Event status to RELEASED and fires EventReleased. Catalog read model is updated. Organizer is notified. 2b. If rejected: Platform Operator provides reason. System sets Event status back to DRAFT with rejection notes. Organizer is notified. |
| **Alternative flows** | A1 — Super Admin may also approve/reject. |
| **Postconditions** | Approved: Event status = RELEASED, visible to Buyers. Rejected: Event status = DRAFT, Organizer notified. |
| **Domain events fired** | `EventReleased` (approved) |

---

### UC-EM-05 — Organizer Cancels Event

| Field | Value |
|---|---|
| **Actor** | Organizer |
| **Aggregate** | Event, Date |
| **Preconditions** | Event exists and belongs to Organizer. |
| **Main flow** | 1. Organizer cancels the Event. 2. System fires EventCanceled and DateCanceled for each Date. 3. All ACTIVE Reservations for the Event are released. 4. All Buyers with Orders in CREATED or PAYMENT_INITIATED status are notified. |
| **Alternative flows** | A1 — Platform Operator or Super Admin may also cancel any Event. |
| **Postconditions** | Event status = CANCELED. Active Reservations released. Buyers notified. |
| **Domain events fired** | `EventCanceled`, `DateCanceled`, `ReservationReleased` |

---

## Seating / Inventory

### UC-SI-01 — Organizer Creates Batch

| Field | Value |
|---|---|
| **Actor** | Organizer |
| **Aggregate** | Batch (child of Seat aggregate) |
| **Preconditions** | Event and Seating Map exist. |
| **Main flow** | 1. Organizer defines a Batch: name, price, currency, Capacity, scheduledStartAt. 2. System creates Batch with status SCHEDULED. 3. At scheduledStartAt, system transitions Batch to ACTIVE and fires BatchActivated. |
| **Alternative flows** | A1 — Organizer sets scheduledStartAt in the past: Batch activates immediately. |
| **Postconditions** | Batch created and scheduled. Transitions to ACTIVE at start time. |
| **Domain events fired** | `BatchCreated`, `BatchScheduled`, `BatchActivated` |

---

### UC-SI-02 — Buyer Selects Seat and Creates Reservation

| Field | Value |
|---|---|
| **Actor** | Buyer |
| **Aggregate** | Seat, Reservation, Batch |
| **Preconditions** | Buyer User is ACTIVE. Event is RELEASED. Date selected. Seat status = AVAILABLE. Batch status = ACTIVE. |
| **Main flow** | 1. Buyer selects a Seat (MAPPED) or quantity (GENERAL_ADMISSION). 2. System atomically: sets Seat status to RESERVED; creates Reservation with expiresAt = now + 10 min; increments Batch.sold. 3. System fires ReservationCreated. 4. Orders context is notified to proceed with checkout. |
| **Alternative flows** | A1 — Seat already RESERVED or SOLD (concurrency): system rejects, Buyer selects another Seat. A2 — Batch.sold = Batch.capacity (Batch EXHAUSTED): system fires BatchExhausted, Buyer cannot proceed with that Batch. |
| **Postconditions** | Seat status = RESERVED. Reservation created with status ACTIVE. Batch.sold incremented. |
| **Domain events fired** | `ReservationCreated`, `BatchExhausted` (if applicable) |

---

### UC-SI-03 — Reservation Expires Without Payment

| Field | Value |
|---|---|
| **Actor** | System (scheduled job) |
| **Aggregate** | Seat, Reservation |
| **Preconditions** | Reservation status = ACTIVE and expiresAt ≤ now. Payment not completed. |
| **Main flow** | 1. Expiration job fires. 2. System sets Reservation status to EXPIRED. 3. System sets Seat status back to AVAILABLE. 4. Batch.sold decremented. 5. System fires ReservationExpired. 6. Orders context transitions Order to EXPIRED. 7. Buyer receives Notification. |
| **Alternative flows** | A1 — Payment arrives simultaneously with expiration: payment processor must check for EXPIRED Order and reject via idempotency check. |
| **Postconditions** | Seat status = AVAILABLE. Reservation status = EXPIRED. Order status = EXPIRED. Buyer notified. |
| **Domain events fired** | `ReservationExpired`, `OrderExpired` |

---

## Orders

### UC-OR-01 — Buyer Initiates Checkout (Order Creation)

| Field | Value |
|---|---|
| **Actor** | Buyer |
| **Aggregate** | Order, LineItem |
| **Preconditions** | Reservation exists with status ACTIVE. Buyer is ACTIVE. |
| **Main flow** | 1. System creates Order with status CREATED, linked to reservationId. 2. System resolves price: Batch price per LineItem + Promotion discount (if applicable) + Service Fee. 3. Order total computed. 4. System fires OrderCreated. 5. Payments context receives OrderCreated to begin payment processing. |
| **Alternative flows** | A1 — Promotion code invalid or exhausted: system rejects the code; Order created without discount. A2 — Reservation expires before Order is confirmed: Order transitions to EXPIRED. |
| **Postconditions** | Order status = CREATED. Total computed. Payments context notified. |
| **Domain events fired** | `OrderCreated` |

---

### UC-OR-02 — Platform Operator Overrides Reservation

| Field | Value |
|---|---|
| **Actor** | Platform Operator |
| **Aggregate** | Order, Reservation (via Seating/Inventory) |
| **Preconditions** | Reservation and Order exist in incident state. |
| **Main flow** | 1. Platform Operator triggers Reservation override (release). 2. System releases the Reservation immediately. 3. Seat returns to AVAILABLE. 4. Order transitions to EXPIRED. 5. AuditLog entry appended. |
| **Alternative flows** | None defined in Phase 0/1. |
| **Postconditions** | Reservation released. Seat available. Order expired. AuditLog written. |
| **Domain events fired** | `ReservationReleased`, `OrderExpired` |

---

## Payments

### UC-PA-01 — Buyer Completes Payment

| Field | Value |
|---|---|
| **Actor** | Buyer |
| **Aggregate** | Payment, Order |
| **Preconditions** | Order status = CREATED. Reservation is still ACTIVE. |
| **Main flow** | 1. Buyer submits payment credentials (card or transfer). 2. System creates Payment with status INITIATED and idempotencyKey. 3. System initiates gateway transaction. 4. Gateway authorizes payment. 5. System sets Payment status to AUTHORIZED. 6. System fires PaymentAuthorized. 7. Orders context updates Order to CONFIRMED. 8. Ticket Issuance is triggered. |
| **Alternative flows** | A1 — Gateway rejects payment: system sets Payment to FAILED, fires PaymentFailed. Seat Reservation is released. Buyer is notified. A2 — Duplicate payment attempt detected by idempotencyKey: system returns existing Payment result without re-processing. |
| **Postconditions** | Payment status = AUTHORIZED. Order status = CONFIRMED. Ticket Issuance triggered. |
| **Domain events fired** | `PaymentInitiated`, `PaymentAuthorized` |

---

### UC-PA-02 — Payment Fails

| Field | Value |
|---|---|
| **Actor** | System (gateway callback) |
| **Aggregate** | Payment, Seat, Reservation |
| **Preconditions** | Payment status = INITIATED. |
| **Main flow** | 1. Gateway returns failure. 2. System sets Payment status to FAILED. 3. Fires PaymentFailed. 4. Seating/Inventory releases Reservation and Seat. 5. Buyer receives Notification. |
| **Alternative flows** | None. |
| **Postconditions** | Payment = FAILED. Seat = AVAILABLE. Reservation = RELEASED. Buyer notified. |
| **Domain events fired** | `PaymentFailed`, `ReservationReleased` |

---

### UC-PA-03 — Payout Generated for Organizer

| Field | Value |
|---|---|
| **Actor** | System (post-payment flow) |
| **Aggregate** | Payout |
| **Preconditions** | PaymentAuthorized received. Order confirmed. |
| **Main flow** | 1. System calculates Payout: grossAmount − serviceFeeTotal = netAmount. 2. System creates Payout with status PENDING. 3. System fires ServiceFeeCalculated, then PayoutGenerated. 4. Payment processor settles netAmount to Organizer. 5. Payout status = PROCESSED. Fires PayoutProcessed. |
| **Alternative flows** | A1 — Settlement fails: Payout status = FAILED. System retries once (retryCount max = 1). If retry fails, fires PayoutFailed — escalated to Finance. |
| **Postconditions** | Payout processed and settled to Organizer, or escalated to Finance on failure. |
| **Domain events fired** | `ServiceFeeCalculated`, `PayoutGenerated`, `PayoutProcessed` / `PayoutFailed` |

---

## Ticket Issuance

### UC-TI-01 — Ticket Issued After Payment

| Field | Value |
|---|---|
| **Actor** | System (triggered by PaymentAuthorized) |
| **Aggregate** | Ticket |
| **Preconditions** | PaymentAuthorized received. Order = CONFIRMED. |
| **Main flow** | 1. System creates Ticket with status ISSUED, qrCode (dynamic, 2-min TTL), accessPolicy, holderName. 2. seatId populated if MAPPED; null if GENERAL_ADMISSION. 3. System fires TicketIssued. 4. Notifications context delivers Ticket via configured deliveryChannels (EMAIL, PDF, QR, WALLET, DOWNLOAD). 5. Ticket status remains ISSUED. |
| **Alternative flows** | A1 — Duplicate issuance detected via idempotencyKey: system returns existing Ticket without re-issuing. |
| **Postconditions** | Ticket status = ISSUED. Buyer notified and receives Ticket. |
| **Domain events fired** | `TicketIssued`, `TicketDelivered` |

---

### UC-TI-02 — Ticket Canceled by Platform or Organizer

| Field | Value |
|---|---|
| **Actor** | Platform Operator or Organizer |
| **Aggregate** | Ticket |
| **Preconditions** | Ticket status = ISSUED. |
| **Main flow** | 1. Actor triggers Ticket cancelation. 2. System sets Ticket status to CANCELED. 3. AuditLog entry appended. 4. Buyer is notified. |
| **Alternative flows** | A1 — Ticket already USED or INVALIDATED: system rejects cancelation. |
| **Postconditions** | Ticket status = CANCELED. AuditLog written. Buyer notified. |
| **Domain events fired** | `TicketCanceled` |

---

### UC-TI-03 — Support Resends Ticket Manually

| Field | Value |
|---|---|
| **Actor** | Support |
| **Aggregate** | Ticket, Notification |
| **Preconditions** | Ticket status = ISSUED. Buyer requests resend. |
| **Main flow** | 1. Support triggers manual resend from backoffice. 2. System creates new Notification with triggeredBy = manual-resend. 3. Notification dispatched via configured channel. 4. AuditLog entry appended. |
| **Alternative flows** | A1 — Channel delivery fails: Notification status = FAILED. Support may retry. |
| **Postconditions** | Ticket resent to Buyer. Notification logged. AuditLog written. |
| **Domain events fired** | `NotificationDispatched` |

---

### UC-TI-04 — Ticket Invalidated (Fraud Detection)

| Field | Value |
|---|---|
| **Actor** | System (triggered by Access Control) |
| **Aggregate** | Ticket |
| **Preconditions** | Access Control detects fraud (e.g., duplicate scan, conflict). |
| **Main flow** | 1. Access Control fires TicketInvalidated. 2. Ticket Issuance sets Ticket status to INVALIDATED. 3. AuditLog entry appended. 4. Access Control ValidationRecord marks conflictDetected = true. |
| **Alternative flows** | None. |
| **Postconditions** | Ticket status = INVALIDATED. AuditLog written. |
| **Domain events fired** | `TicketInvalidated` |

---

## Access Control

### UC-AC-01 — Door Validator Scans QR — Success Path

| Field | Value |
|---|---|
| **Actor** | Door Validator |
| **Aggregate** | ValidationRecord, Ticket (read-only) |
| **Preconditions** | Door Validator device is online. Ticket status = ISSUED. QR is valid and not expired. |
| **Main flow** | 1. Door Validator scans QR code. 2. System performs real-time lookup of Ticket within 100 ms. 3. System verifies: Ticket status = ISSUED; QR not expired; Event/Date matches; accessPolicy allows entry. 4. System creates ValidationRecord with result = SUCCEEDED. 5. System fires ValidationSucceeded. 6. Ticket Issuance sets Ticket status to USED. |
| **Alternative flows** | None (failures covered in UC-AC-02). |
| **Postconditions** | ValidationRecord written (result = SUCCEEDED). Ticket status = USED. Entry granted. |
| **Domain events fired** | `ValidationAttempted`, `ValidationSucceeded`, `TicketCheckedIn` |

---

### UC-AC-02 — Door Validator Scans QR — Failure Path

| Field | Value |
|---|---|
| **Actor** | Door Validator |
| **Aggregate** | ValidationRecord |
| **Preconditions** | Door Validator device is online. QR presented at door. |
| **Main flow** | 1. Door Validator scans QR code. 2. System performs lookup. 3. Validation fails due to one of: ALREADY_USED, WRONG_EVENT, EXPIRED, INVALIDATED. 4. System creates ValidationRecord with result = FAILED and failureReason. 5. System fires ValidationFailed with reason. 6. Entry is denied. |
| **Alternative flows** | None. |
| **Postconditions** | ValidationRecord written (result = FAILED). Ticket status unchanged. Entry denied. |
| **Domain events fired** | `ValidationAttempted`, `ValidationFailed(reason)` |

---

### UC-AC-03 — Offline Validator Syncs After Reconnection

| Field | Value |
|---|---|
| **Actor** | System (Validator device reconnects) |
| **Aggregate** | ValidationRecord |
| **Preconditions** | Validator device was offline. Scans were queued locally (isOffline = true). Device reconnects. |
| **Main flow** | 1. Device fires ValidatorSyncRequested. 2. System processes queued ValidationRecords in order. 3. For each: if Ticket not yet USED, ValidationRecord created with result = SUCCEEDED, syncedAt = now; Ticket set to USED. 4. If Ticket is already USED (conflict): ValidationRecord created with result = FAILED, failureReason = ALREADY_USED, conflictDetected = true. System fires ConflictDetected. Entry denied retroactively. 5. Fires ValidatorSynced when all records reconciled. |
| **Alternative flows** | A1 — Sync fails mid-way: system retries from last unprocessed record. |
| **Postconditions** | All queued ValidationRecords persisted. Conflicts detected and rejected. ValidatorSynced fired. |
| **Domain events fired** | `ValidatorSyncRequested`, `ValidationAttempted`, `ConflictDetected` (if applicable), `ValidatorSynced` |

---

## Notifications

### UC-NO-01 — System Dispatches Event-Driven Notification

| Field | Value |
|---|---|
| **Actor** | System (triggered by domain events) |
| **Aggregate** | Notification |
| **Preconditions** | A domain event that requires Notification is fired (e.g., TicketIssued, ReservationExpired, PaymentFailed). |
| **Main flow** | 1. Notifications context receives domain event. 2. System creates Notification with channel (EMAIL / SMS / WHATSAPP), templateId, payload, and triggeredBy. 3. System dispatches Notification via provider. 4. On success: status = DELIVERED; fires NotificationDelivered. 5. On failure: status = FAILED; fires NotificationFailed; retryCount incremented. |
| **Alternative flows** | A1 — Max retries exceeded: Notification remains FAILED. Support may trigger manual resend (UC-TI-03). |
| **Postconditions** | Notification status = DELIVERED or FAILED. |
| **Domain events fired** | `NotificationDispatched`, `NotificationDelivered` / `NotificationFailed` |

---

## Reporting

### UC-RE-01 — Finance Views Financial Reports

| Field | Value |
|---|---|
| **Actor** | Finance |
| **Aggregate** | SalesReport, CommissionReport (read models) |
| **Preconditions** | Finance User is ACTIVE. |
| **Main flow** | 1. Finance accesses Reporting panel. 2. System surfaces SalesReport and CommissionReport projections for all Organizers. 3. Finance exports settlement data. |
| **Alternative flows** | None. |
| **Postconditions** | Finance receives report data. No write to any aggregate. |
| **Domain events fired** | None. |

---

### UC-RE-02 — Organizer Views Own Sales Reports

| Field | Value |
|---|---|
| **Actor** | Organizer |
| **Aggregate** | SalesReport (read model) |
| **Preconditions** | Organizer User is ACTIVE. |
| **Main flow** | 1. Organizer accesses Reporting section of organizer panel. 2. System returns SalesReport scoped to organizerId only. 3. Organizer views Ticket sales, revenue, and Payout data for own Events. |
| **Alternative flows** | A1 — Organizer attempts to access another Organizer's data: system rejects with authorization error. |
| **Postconditions** | Organizer views own scoped report. |
| **Domain events fired** | None. |

---

### UC-RE-03 — Venue Staff Views Access Reports

| Field | Value |
|---|---|
| **Actor** | Venue Staff |
| **Aggregate** | ValidationRecord (via Reporting read model) |
| **Preconditions** | Venue Staff User is ACTIVE and scoped to an Organizer. |
| **Main flow** | 1. Venue Staff accesses access/validation report for an Event. 2. System returns ValidationRecord projections scoped to that Organizer's Events. 3. Venue Staff reviews entry counts, failure reasons, and sync states. |
| **Alternative flows** | None. |
| **Postconditions** | Venue Staff views access report. No write. |
| **Domain events fired** | None. |
