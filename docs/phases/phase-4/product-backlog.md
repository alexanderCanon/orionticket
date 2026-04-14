# Product Backlog

> **Phase:** 4 — Backlog & Planning  
> **Source:** `docs/phases/phase-2/functional-requirements.md`, `docs/phases/phase-2/use-case-catalog.md`, `docs/phases/phase-0/discovery.md` Block 12  
> **Format:** User stories grouped by sprint. Prioritized by dependency order and business value.

---

## Sprint 1 — Foundation: Identity, Event Management, Basic Organizer Panel

*Days 8–17 (10 working days)*

---

### US-001 — Buyer Self-Registration

| Field | Value |
|---|---|
| **Story** | As a **Buyer** I want to **register with email, password, name, and phone** so that **I can purchase Tickets on the platform**. |
| **Bounded context** | Identity |
| **Priority** | Critical |
| **Dependencies** | None |
| **Estimated complexity** | M |
| **Use case** | UC-ID-01 |

**Acceptance criteria:**
- POST `/v1/auth/register` creates User with status UNVERIFIED.
- Duplicate email returns 409.
- Verification flow sets status to ACTIVE.
- Password stored as hash, never plaintext.

---

### US-002 — User Authentication (Login)

| Field | Value |
|---|---|
| **Story** | As a **User** I want to **log in with email and password** so that **I receive a JWT token to access protected resources**. |
| **Bounded context** | Identity |
| **Priority** | Critical |
| **Dependencies** | US-001 |
| **Estimated complexity** | M |
| **Use case** | UC-ID-01 (prerequisite) |

**Acceptance criteria:**
- POST `/v1/auth/login` returns JWT with userId, role, organizerId, expiresAt.
- Invalid credentials return 401.
- SUSPENDED or UNVERIFIED users return 403.
- JWT validated by Spring Cloud Gateway on all protected routes.

---

### US-003 — Super Admin User and Role Management

| Field | Value |
|---|---|
| **Story** | As a **Super Admin** I want to **create, update, and suspend Users and manage Roles** so that **I can control platform access for all Organizers and staff**. |
| **Bounded context** | Identity |
| **Priority** | High |
| **Dependencies** | US-001, US-002 |
| **Estimated complexity** | L |
| **Use case** | UC-ID-03 |

**Acceptance criteria:**
- CRUD endpoints for Users and Roles.
- Role assignment with permissions array.
- AuditLog entry on every change.
- Super Admin can view and manage Users across all Organizers.

---

### US-004 — Organizer Creates Event with Dates

| Field | Value |
|---|---|
| **Story** | As an **Organizer** I want to **create an Event with one or more Dates** so that **I can define what I am selling on the platform**. |
| **Bounded context** | Event Management |
| **Priority** | Critical |
| **Dependencies** | US-002 |
| **Estimated complexity** | M |
| **Use case** | UC-EM-01 |

**Acceptance criteria:**
- POST `/v1/events` creates Event in DRAFT status.
- POST `/v1/events/{eventId}/dates` adds a Date with scheduledAt, venueId, capacity.
- EventCreated and DateAdded domain events published to RabbitMQ.
- Only the owning Organizer can access their Events.

---

### US-005 — Organizer Creates Venue

| Field | Value |
|---|---|
| **Story** | As an **Organizer** I want to **create a Venue with name, address, and capacity** so that **I can assign it to my Event Dates**. |
| **Bounded context** | Event Management |
| **Priority** | Critical |
| **Dependencies** | US-002 |
| **Estimated complexity** | S |
| **Use case** | UC-EM-02 (prerequisite) |

**Acceptance criteria:**
- POST endpoint creates Venue scoped to Organizer.
- VenueCreated event published.
- Venue listed in Organizer panel.

---

### US-006 — Organizer Submits Event for Review

| Field | Value |
|---|---|
| **Story** | As an **Organizer** I want to **submit my Event for review** so that **the Platform Operator can approve it for public visibility**. |
| **Bounded context** | Event Management |
| **Priority** | Critical |
| **Dependencies** | US-004, US-005 |
| **Estimated complexity** | S |
| **Use case** | UC-EM-03 |

**Acceptance criteria:**
- POST `/v1/events/{eventId}/submit` transitions Event to UNDER_REVIEW.
- Event without Dates or without Batch returns 422.
- EventSubmittedForReview event published.
- Platform Operator notified.

---

### US-007 — Platform Operator Approves or Rejects Event

| Field | Value |
|---|---|
| **Story** | As a **Platform Operator** I want to **approve or reject submitted Events** so that **only valid Events are visible to Buyers**. |
| **Bounded context** | Event Management |
| **Priority** | Critical |
| **Dependencies** | US-006 |
| **Estimated complexity** | M |
| **Use case** | UC-EM-04 |

**Acceptance criteria:**
- POST `/v1/events/{eventId}/approve` sets status to RELEASED. EventReleased published.
- POST `/v1/events/{eventId}/reject` sets status back to DRAFT with rejection reason.
- Only Platform Operator or Super Admin can approve/reject.
- Organizer notified of approval or rejection.

---

### US-008 — Cancel Event

| Field | Value |
|---|---|
| **Story** | As an **Organizer** I want to **cancel my Event** so that **it is no longer available for purchase and Buyers are notified**. |
| **Bounded context** | Event Management |
| **Priority** | High |
| **Dependencies** | US-004 |
| **Estimated complexity** | M |
| **Use case** | UC-EM-05 |

**Acceptance criteria:**
- POST `/v1/events/{eventId}/cancel` fires EventCanceled and DateCanceled for all Dates.
- Active Reservations for the Event are released (via event to Seating/Inventory).
- Buyers with pending Orders notified.

---

### US-009 — Organizer Staff Management

| Field | Value |
|---|---|
| **Story** | As an **Organizer** I want to **create Venue Staff and Door Validator accounts scoped to my organization** so that **my operational team can access the platform**. |
| **Bounded context** | Identity |
| **Priority** | High |
| **Dependencies** | US-002, US-003 |
| **Estimated complexity** | M |
| **Use case** | UC-ID-02 |

**Acceptance criteria:**
- POST `/v1/organizers/{organizerId}/staff` creates User with VENUE_STAFF or DOOR_VALIDATOR role.
- Role restricted to scoped roles only (not Support, Finance, etc.) — returns 422 otherwise.
- New User receives credential Notification.

---

### US-010 — Basic Organizer Panel (Frontend)

| Field | Value |
|---|---|
| **Story** | As an **Organizer** I want to **access a web panel to create Events, manage Dates, create Venues, and submit for review** so that **I can operate my events on the platform**. |
| **Bounded context** | Event Management (frontend) |
| **Priority** | Critical |
| **Dependencies** | US-002, US-004, US-005, US-006, US-007 |
| **Estimated complexity** | L |
| **Use case** | UC-EM-01 through UC-EM-04 (frontend) |

**Acceptance criteria:**
- Angular app with login, event list, event create/edit form, date management, venue management.
- Submit for review button visible in DRAFT status.
- Event status displayed (DRAFT, UNDER_REVIEW, RELEASED, CANCELED).
- Responsive web layout.

---

### US-011 — Basic Super Admin Panel (Frontend)

| Field | Value |
|---|---|
| **Story** | As a **Super Admin** I want to **access a panel to manage Users, Roles, and approve Events** so that **I can operate the platform**. |
| **Bounded context** | Identity + Event Management (frontend) |
| **Priority** | High |
| **Dependencies** | US-003, US-007 |
| **Estimated complexity** | L |
| **Use case** | UC-ID-03, UC-EM-04 (frontend) |

**Acceptance criteria:**
- User list with create/edit/suspend actions.
- Role management with permissions.
- Event approval queue with approve/reject actions.
- Full platform visibility (all Organizers).

---

### US-012 — Public Event Catalog

| Field | Value |
|---|---|
| **Story** | As a **Buyer** I want to **browse released Events with filters** so that **I can discover events to attend**. |
| **Bounded context** | Event Management (Catalog read model) |
| **Priority** | High |
| **Dependencies** | US-007 |
| **Estimated complexity** | M |
| **Use case** | UC-SI-02 (discovery step) |

**Acceptance criteria:**
- GET `/v1/catalog/events` returns only RELEASED Events.
- Filters: category, date, organizer.
- Pagination supported.
- Public endpoint — no auth required.

---

## Sprint 2 — Core Transaction: Seating/Inventory, Orders, Payments

*Days 18–27 (10 working days)*

---

### US-013 — Organizer Configures Seating Map

| Field | Value |
|---|---|
| **Story** | As an **Organizer** I want to **configure a Seating Map with Zones, Sections, Rows, and Seats** so that **Buyers can select specific seats for my Event**. |
| **Bounded context** | Seating / Inventory |
| **Priority** | Critical |
| **Dependencies** | US-004, US-005 |
| **Estimated complexity** | XL |
| **Use case** | UC-EM-02 |

**Acceptance criteria:**
- POST `/v1/events/{eventId}/dates/{dateId}/seating-map` creates Seat records with status AVAILABLE.
- Supports both MAPPED and GENERAL_ADMISSION types.
- SeatingMapConfigured event published.
- Seating Map locked after Event is RELEASED (409 on modification).

---

### US-014 — Organizer Creates Batch

| Field | Value |
|---|---|
| **Story** | As an **Organizer** I want to **create a Batch with price, capacity, and scheduled start time** so that **I can control ticket pricing and availability phases**. |
| **Bounded context** | Seating / Inventory |
| **Priority** | Critical |
| **Dependencies** | US-013 |
| **Estimated complexity** | M |
| **Use case** | UC-SI-01 |

**Acceptance criteria:**
- POST `/v1/events/{eventId}/dates/{dateId}/batches` creates Batch with status SCHEDULED.
- Batch transitions to ACTIVE at scheduledStartAt. BatchActivated published.
- BatchExhausted published when sold = capacity.
- Currency defaults to GTQ.

---

### US-015 — Buyer Views Seat Availability

| Field | Value |
|---|---|
| **Story** | As a **Buyer** I want to **view available seats for an Event Date** so that **I can choose where I want to sit**. |
| **Bounded context** | Seating / Inventory |
| **Priority** | Critical |
| **Dependencies** | US-013, US-014 |
| **Estimated complexity** | M |
| **Use case** | UC-SI-02 (view step) |

**Acceptance criteria:**
- GET `/v1/events/{eventId}/dates/{dateId}/seats` returns Seat list with status, zone, section, row, price.
- Only AVAILABLE seats selectable.
- Batch price and name included per seat.

---

### US-016 — Buyer Selects Seat and Creates Reservation

| Field | Value |
|---|---|
| **Story** | As a **Buyer** I want to **select a Seat and hold it for 10 minutes** so that **I have time to complete payment without losing my seat**. |
| **Bounded context** | Seating / Inventory |
| **Priority** | Critical |
| **Dependencies** | US-015, US-002 |
| **Estimated complexity** | XL |
| **Use case** | UC-SI-02 |

**Acceptance criteria:**
- POST `/v1/reservations` atomically: Seat → RESERVED, Reservation created (expiresAt = +10 min), Batch.sold incremented.
- Concurrent request for same Seat returns 409.
- Batch EXHAUSTED returns 410.
- ReservationCreated event published to RabbitMQ.
- Pessimistic or distributed lock on Seat row to prevent race condition.

---

### US-017 — Reservation Expires Without Payment

| Field | Value |
|---|---|
| **Story** | As the **System** I want to **automatically expire Reservations after 10 minutes** so that **unpaid Seats return to inventory for other Buyers**. |
| **Bounded context** | Seating / Inventory |
| **Priority** | Critical |
| **Dependencies** | US-016 |
| **Estimated complexity** | L |
| **Use case** | UC-SI-03 |

**Acceptance criteria:**
- Scheduled job detects Reservation.expiresAt ≤ now.
- Reservation → EXPIRED, Seat → AVAILABLE, Batch.sold decremented.
- ReservationExpired published. Orders context transitions Order to EXPIRED.
- Buyer receives Notification.

---

### US-018 — Buyer Initiates Checkout (Order Creation)

| Field | Value |
|---|---|
| **Story** | As a **Buyer** I want to **proceed to checkout after selecting my seat** so that **an Order is created with the correct price, Service Fee, and total**. |
| **Bounded context** | Orders |
| **Priority** | Critical |
| **Dependencies** | US-016 |
| **Estimated complexity** | L |
| **Use case** | UC-OR-01 |

**Acceptance criteria:**
- POST `/v1/orders` creates Order with status CREATED, linked to reservationId.
- Price resolved from Batch price. Service Fee calculated. Total computed.
- Promotion code applied if provided (invalid code → Order created without discount, 422 on code, not on Order).
- OrderCreated event published. Payments context receives it.
- Idempotency key validated (duplicate returns existing Order).

---

### US-019 — Buyer Completes Payment

| Field | Value |
|---|---|
| **Story** | As a **Buyer** I want to **submit payment by card or bank transfer** so that **my Order is confirmed and my Ticket is issued**. |
| **Bounded context** | Payments |
| **Priority** | Critical |
| **Dependencies** | US-018 |
| **Estimated complexity** | XL |
| **Use case** | UC-PA-01 |

**Acceptance criteria:**
- POST `/v1/payments` creates Payment with status INITIATED, idempotencyKey generated.
- Gateway integration processes the charge (tokenized — no raw card data stored).
- On gateway success: Payment → AUTHORIZED, PaymentAuthorized published, Order → CONFIRMED.
- Webhook endpoint (`POST /v1/payments/webhook`) handles gateway callbacks.
- Duplicate payment detected by idempotencyKey returns existing result.

---

### US-020 — Payment Failure Handling

| Field | Value |
|---|---|
| **Story** | As the **System** I want to **handle payment failures gracefully** so that **the Seat is released and the Buyer is notified**. |
| **Bounded context** | Payments |
| **Priority** | Critical |
| **Dependencies** | US-019 |
| **Estimated complexity** | M |
| **Use case** | UC-PA-02 |

**Acceptance criteria:**
- On gateway failure: Payment → FAILED, PaymentFailed published.
- Seating/Inventory releases Reservation and Seat.
- Buyer receives Notification of failure.

---

### US-021 — Platform Operator Overrides Reservation

| Field | Value |
|---|---|
| **Story** | As a **Platform Operator** I want to **manually release a Reservation** so that **I can resolve operational incidents where a Seat is incorrectly held**. |
| **Bounded context** | Seating / Inventory + Orders |
| **Priority** | Medium |
| **Dependencies** | US-016 |
| **Estimated complexity** | S |
| **Use case** | UC-OR-02 |

**Acceptance criteria:**
- DELETE `/v1/reservations/{reservationId}` releases Reservation and Seat.
- Only Platform Operator or Super Admin authorized.
- AuditLog entry written.

---

### US-022 — Checkout Flow (Frontend)

| Field | Value |
|---|---|
| **Story** | As a **Buyer** I want to **see a checkout page with seat selection, order summary, and payment form** so that **I can complete my purchase in a single flow**. |
| **Bounded context** | Seating/Inventory + Orders + Payments (frontend) |
| **Priority** | Critical |
| **Dependencies** | US-015, US-016, US-018, US-019 |
| **Estimated complexity** | XL |
| **Use case** | UC-SI-02, UC-OR-01, UC-PA-01 (frontend) |

**Acceptance criteria:**
- Seat map or list displayed. Selection triggers Reservation.
- 10-minute countdown timer visible.
- Order summary shows subtotal, discount, Service Fee, total.
- Payment form accepts card or transfer. Redirects/shows confirmation on success.
- Error states for concurrency conflict, payment failure, expiration.

---

## Sprint 3 — Ticket Lifecycle: Ticket Issuance, Notifications, Buyer Portal

*Days 28–37 (10 working days)*

---

### US-023 — Ticket Issued After Payment

| Field | Value |
|---|---|
| **Story** | As the **System** I want to **issue a Ticket with a dynamic QR code after payment is confirmed** so that **the Buyer receives their proof of entry**. |
| **Bounded context** | Ticket Issuance |
| **Priority** | Critical |
| **Dependencies** | US-019 |
| **Estimated complexity** | L |
| **Use case** | UC-TI-01 |

**Acceptance criteria:**
- Consumes PaymentAuthorized event from RabbitMQ.
- Creates Ticket with status ISSUED, dynamic qrCode (2-min TTL), accessPolicy.
- seatId populated for MAPPED, null for GENERAL_ADMISSION.
- TicketIssued event published. Notifications context delivers Ticket.
- Idempotency key prevents double issuance.

---

### US-024 — Cancel Ticket

| Field | Value |
|---|---|
| **Story** | As a **Platform Operator or Organizer** I want to **cancel an issued Ticket** so that **it can no longer be used for entry**. |
| **Bounded context** | Ticket Issuance |
| **Priority** | High |
| **Dependencies** | US-023 |
| **Estimated complexity** | S |
| **Use case** | UC-TI-02 |

**Acceptance criteria:**
- POST `/v1/tickets/{ticketId}/cancel` sets Ticket status to CANCELED.
- Ticket in USED or INVALIDATED status cannot be canceled (409).
- TicketCanceled event published. Buyer notified.
- AuditLog entry written.

---

### US-025 — Support Resends Ticket

| Field | Value |
|---|---|
| **Story** | As **Support** I want to **manually resend a Ticket to a Buyer** so that **they can receive their Ticket if the original delivery failed**. |
| **Bounded context** | Ticket Issuance + Notifications |
| **Priority** | Medium |
| **Dependencies** | US-023 |
| **Estimated complexity** | S |
| **Use case** | UC-TI-03 |

**Acceptance criteria:**
- POST `/v1/tickets/{ticketId}/resend` triggers new Notification delivery.
- Ticket status unchanged. AuditLog entry written.
- Only Support or Super Admin authorized.

---

### US-026 — Event-Driven Notification Dispatch

| Field | Value |
|---|---|
| **Story** | As the **System** I want to **automatically send Notifications via Email/SMS/WhatsApp when domain events fire** so that **Users are informed at every stage of their journey**. |
| **Bounded context** | Notifications |
| **Priority** | Critical |
| **Dependencies** | US-004 (EventSubmittedForReview), US-016 (ReservationCreated), US-017 (ReservationExpired), US-019 (PaymentAuthorized), US-020 (PaymentFailed), US-023 (TicketIssued) |
| **Estimated complexity** | L |
| **Use case** | UC-NO-01 |

**Acceptance criteria:**
- Consumes domain events. Creates Notification with channel, templateId, payload, triggeredBy.
- Dispatches via Email provider (v1 minimum). SMS/WhatsApp integration optional for v1 demo.
- On failure: status FAILED, retryCount incremented, NotificationFailed published.
- Dead-letter queue configured for undeliverable messages.

---

### US-027 — Buyer Portal (Frontend)

| Field | Value |
|---|---|
| **Story** | As a **Buyer** I want to **view my purchased Tickets, download them, and see my order history** so that **I can manage my event attendance**. |
| **Bounded context** | Ticket Issuance + Orders (frontend) |
| **Priority** | High |
| **Dependencies** | US-023, US-018 |
| **Estimated complexity** | L |
| **Use case** | UC-TI-01, UC-OR-01 (frontend) |

**Acceptance criteria:**
- Ticket list with QR display (refreshes dynamically).
- Order history with status and total.
- Download Ticket as PDF.
- Responsive web — no native app (v1).

---

### US-028 — Payout Generation

| Field | Value |
|---|---|
| **Story** | As the **System** I want to **generate a Payout for the Organizer after the Event Date passes** so that **the Organizer receives their settlement**. |
| **Bounded context** | Payments |
| **Priority** | High |
| **Dependencies** | US-019 |
| **Estimated complexity** | L |
| **Use case** | UC-PA-03 |

**Acceptance criteria:**
- Scheduled job detects Date.scheduledAt has passed.
- Aggregates confirmed Payments: grossAmount − serviceFeeTotal = netAmount.
- Creates Payout with status PENDING. PayoutGenerated published.
- On success: PayoutProcessed. On failure: retries once, then PayoutFailed.

---

## Sprint 4 — Operations: Access Control, Reporting, Stabilization

*Days 38–45 (8 working days)*

---

### US-029 — Door Validator Scans QR — Success

| Field | Value |
|---|---|
| **Story** | As a **Door Validator** I want to **scan a QR code and verify entry** so that **only valid Ticket holders enter the venue**. |
| **Bounded context** | Access Control |
| **Priority** | Critical |
| **Dependencies** | US-023 |
| **Estimated complexity** | L |
| **Use case** | UC-AC-01 |

**Acceptance criteria:**
- POST `/v1/validations` calls Ticket Issuance to verify Ticket (synchronous, ≤ 100 ms target).
- Ticket ISSUED + QR valid + Event matches → ValidationRecord result = SUCCEEDED.
- Ticket status set to USED. TicketCheckedIn published.
- Response includes result and validation details.

---

### US-030 — Door Validator Scans QR — Failure

| Field | Value |
|---|---|
| **Story** | As a **Door Validator** I want to **see a clear rejection reason when a QR scan fails** so that **I can inform the Buyer and take appropriate action**. |
| **Bounded context** | Access Control |
| **Priority** | Critical |
| **Dependencies** | US-029 |
| **Estimated complexity** | M |
| **Use case** | UC-AC-02 |

**Acceptance criteria:**
- Failure reasons: ALREADY_USED, WRONG_EVENT, EXPIRED, INVALIDATED.
- ValidationRecord created with result = FAILED and failureReason.
- ValidationFailed event published.
- Entry denied — clear message displayed to Door Validator.

---

### US-031 — Offline Validator Sync

| Field | Value |
|---|---|
| **Story** | As a **Door Validator device** I want to **sync queued scans after reconnecting** so that **offline validations are reconciled and conflicts detected**. |
| **Bounded context** | Access Control |
| **Priority** | High |
| **Dependencies** | US-029 |
| **Estimated complexity** | XL |
| **Use case** | UC-AC-03 |

**Acceptance criteria:**
- POST `/v1/validations/sync` processes queued records in chronological order.
- First-scan-wins: if Ticket already USED, record FAILED with conflictDetected = true.
- ConflictDetected event published for each conflict.
- ValidatorSynced event published when all records processed.

---

### US-032 — Validator App (Frontend)

| Field | Value |
|---|---|
| **Story** | As a **Door Validator** I want to **use a mobile/tablet web app to scan QR codes** so that **I can validate Tickets at the venue entrance**. |
| **Bounded context** | Access Control (frontend) |
| **Priority** | Critical |
| **Dependencies** | US-029, US-030 |
| **Estimated complexity** | L |
| **Use case** | UC-AC-01, UC-AC-02 (frontend) |

**Acceptance criteria:**
- Camera-based QR scanner (web API).
- Clear GRANTED / DENIED visual result.
- Failure reason displayed.
- Offline queue with visual indicator of queued scans.
- Sync button when connectivity restored.

---

### US-033 — Finance Views Financial Reports

| Field | Value |
|---|---|
| **Story** | As a **Finance** user I want to **view Sales and Commission Reports** so that **I can track platform revenue and Organizer settlements**. |
| **Bounded context** | Reporting |
| **Priority** | High |
| **Dependencies** | US-019, US-028 |
| **Estimated complexity** | M |
| **Use case** | UC-RE-01 |

**Acceptance criteria:**
- GET `/v1/reports/sales` returns SalesReport projections.
- GET `/v1/reports/commissions` returns CommissionReport projections.
- Reports populated via event projections (TicketIssued, PaymentAuthorized, PayoutProcessed).
- Data is eventually consistent — no direct DB queries to other services.

---

### US-034 — Organizer Views Own Sales Reports

| Field | Value |
|---|---|
| **Story** | As an **Organizer** I want to **view my sales reports scoped to my own Events** so that **I can track my ticket sales and revenue**. |
| **Bounded context** | Reporting |
| **Priority** | High |
| **Dependencies** | US-033 |
| **Estimated complexity** | S |
| **Use case** | UC-RE-02 |

**Acceptance criteria:**
- GET `/v1/reports/sales?organizerId={id}` returns only the Organizer's data.
- Attempting to query another Organizer's data returns 403.
- Report displayed in Organizer panel.

---

### US-035 — Venue Staff Views Access Reports

| Field | Value |
|---|---|
| **Story** | As **Venue Staff** I want to **view access/validation reports for my Events** so that **I can monitor door entry operations**. |
| **Bounded context** | Reporting |
| **Priority** | Medium |
| **Dependencies** | US-029, US-033 |
| **Estimated complexity** | S |
| **Use case** | UC-RE-03 |

**Acceptance criteria:**
- GET `/v1/reports/access?eventId={id}` returns validation summary.
- Includes total validations, succeeded, failed, failure breakdown, offline scans, conflicts.
- Scoped to own Organizer's Events.

---

### US-036 — Ticket Invalidation (Fraud Detection)

| Field | Value |
|---|---|
| **Story** | As the **System** I want to **invalidate a Ticket when fraud is detected** so that **the Ticket cannot be used for entry**. |
| **Bounded context** | Ticket Issuance (triggered by Access Control) |
| **Priority** | High |
| **Dependencies** | US-029, US-031 |
| **Estimated complexity** | M |
| **Use case** | UC-TI-04 |

**Acceptance criteria:**
- Consumes ConflictDetected event from Access Control.
- Sets Ticket status to INVALIDATED. TicketInvalidated published.
- AuditLog entry written.

---

### US-037 — End-to-End Integration and Stabilization

| Field | Value |
|---|---|
| **Story** | As the **Team** I want to **verify the full purchase-to-entry flow works end-to-end** so that **we can demo a complete cycle on Day 45**. |
| **Bounded context** | All |
| **Priority** | Critical |
| **Dependencies** | US-001 through US-032 |
| **Estimated complexity** | L |
| **Use case** | All critical flows |

**Acceptance criteria:**
- Demo scenario passes: one Organizer → one Event → one Buyer purchases → Door Validator scans QR → entry granted.
- All 9 services start with `docker-compose up` without errors.
- Spring Cloud Gateway routes all requests correctly.
- RabbitMQ events flow through the full chain.
- No data loss or orphaned records in the flow.

---

## BACKLOG — v2

Items explicitly excluded from MVP (discovery.md Block 12.4).

| ID | Story | Reason for deferral |
|---|---|---|
| US-V2-001 | As a Buyer I want to request a refund so that I can recover my money if I cannot attend. | No refunds in v1 (ADR-002). |
| US-V2-002 | As a Buyer I want to transfer my Ticket to another person so that someone else can attend in my place. | No transfer or resale in v1 (ADR-003). |
| US-V2-003 | As a Buyer I want to add Tickets from multiple Events to a single cart so that I can check out once. | Multi-event cart excluded from v1 (discovery.md Block 4.3). |
| US-V2-004 | As a Buyer I want to use the platform via a native mobile app so that I have a better mobile experience. | Responsive web only in v1 (discovery.md Block 4.9). |
| US-V2-005 | As a Marketing user I want CRM and marketing automation so that I can run targeted campaigns. | CRM not in scope (discovery.md Block 5.8). |
| US-V2-006 | As an Organizer I want to sell Tickets through third-party channels so that I can reach more Buyers. | External sales channels excluded from v1 (discovery.md Block 9.5). |
| US-V2-007 | As a Finance user I want to generate tax receipts and invoices so that Organizers have billing documents. | Billing/invoicing not in scope for v1 (discovery.md Block 6.6). |
| US-V2-008 | As a Partner I want to integrate via a public API so that I can build on top of OrionTicket. | Advanced partner API deferred to v2 (discovery.md Block 10.9). |
| US-V2-009 | As an Organizer I want a highly customizable admin panel so that I can tailor the experience to my brand. | Custom organizer panel beyond basic deferred to v2 (discovery.md Block 10.9). |
| US-V2-010 | As a Platform Operator I want social media integrations so that Events can be promoted on social platforms. | Social media integrations deferred to v2 (discovery.md Block 10.9). |
