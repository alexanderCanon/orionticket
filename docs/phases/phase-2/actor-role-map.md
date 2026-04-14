# Actor–Role Map

> **Phase:** 2 — Use Cases & Flows  
> **Source:** `docs/phases/phase-0/discovery.md` (Block 5, Block 11, Block 12), `docs/phases/phase-1/bounded-context-diagrams.md`, `docs/phases/phase-1/aggregate-definitions.md`  
> **Constraint:** Only actors and permissions documented in Phase 0 and Phase 1 are represented here.

---

## Actors defined in the system

| Actor | Type | Scope |
|---|---|---|
| Buyer | External user | Own Orders, Tickets, Reservations |
| Organizer | External user (multi-tenant) | Own Events, Dates, Venues, Batches, Promotions, staff, buyers |
| Door Validator | Operational staff | QR scan and Validation only |
| Venue Staff | Operational staff | Coordinate Door Validators; view access/validation reports |
| Support | Internal platform staff | View and respond to buyer issues; resend Tickets; escalate |
| Finance | Internal platform staff | View all financial reports; export settlement data |
| Marketing | Internal platform staff | View buyer/event analytics; manage Promotions; export segments |
| Platform Operator | Internal platform staff | View all platform reports; configure platform settings; approve/reject Events |
| Super Admin | Internal platform staff | Full platform control — all modules, all Organizers, all data |

---

## Actor ↔ Bounded Context ↔ Aggregate mapping

### Buyer

| Bounded Context | Aggregates | Operation |
|---|---|---|
| Identity | User | Create (self-register); Read (own profile) |
| Event Management | Event, Date | Read (discover events, view dates) |
| Seating / Inventory | Seat, Reservation, Batch | Read (view availability); Write (create Reservation via seat selection) |
| Orders | Order, LineItem | Write (create Order at checkout); Read (view own Orders) |
| Payments | Payment | Write (initiate Payment); Read (view own payment status) |
| Ticket Issuance | Ticket | Read (view, download own Tickets) |
| Notifications | Notification | Read (receives delivery of Tickets and status updates) |

**Permission scope (Discovery Block 5):** View purchases, download Tickets, contact Support. No transfer, no refund.

---

### Organizer

| Bounded Context | Aggregates | Operation |
|---|---|---|
| Identity | User | Read (own profile); Write (manage own Venue Staff and Door Validators — create/suspend users scoped to own Organizer) |
| Event Management | Event, Date | Write (create, update, submit Event and Dates for review; cancel own Events) |
| Seating / Inventory | Seat, Batch | Write (configure Seating Maps, create and schedule Batches, set Capacity) |
| Orders | Order | Read (view own sales orders) |
| Payments | Payout | Read (view own Payout status and settlement history) |
| Ticket Issuance | Ticket | Read (view issued Tickets for own events) |
| Notifications | Notification | Read (view notification logs for own events) |
| Reporting | SalesReport, CommissionReport | Read (view own Sales and Commission Reports) |

**Permission scope (Discovery Block 5):** Create and manage own events; configure Venue and Seating Maps; set prices, Batches, Promotions; view own sales reports; manage own staff; manage own buyers.

---

### Door Validator

| Bounded Context | Aggregates | Operation |
|---|---|---|
| Access Control | ValidationRecord | Write (create ValidationRecord on QR scan — success or failure path) |
| Ticket Issuance | Ticket | Read (QR lookup to verify Ticket identity; no write) |

**Permission scope (Discovery Block 5):** Scan and validate QR at entry only.

---

### Venue Staff

| Bounded Context | Aggregates | Operation |
|---|---|---|
| Identity | User | Read (view Door Validator accounts scoped to own Organizer) |
| Access Control | ValidationRecord | Read (view access and validation reports) |

**Permission scope (Discovery Block 5):** Coordinate Door Validators; view access/validation reports.

---

### Support

| Bounded Context | Aggregates | Operation |
|---|---|---|
| Identity | User | Read (view buyer accounts) |
| Orders | Order | Read (view order details) |
| Ticket Issuance | Ticket | Read (view Ticket details); Write (trigger manual resend — new delivery action) |
| Notifications | Notification | Write (trigger manual resend); Read (view delivery logs) |
| Reporting | SalesReport | Read (view reports to support resolution) |

**Permission scope (Discovery Block 5):** View and respond to buyer tickets; resend Tickets manually; view order details; escalate to Platform Operator.

---

### Finance

| Bounded Context | Aggregates | Operation |
|---|---|---|
| Payments | Payment, Payout | Read (view all Payment and Payout records) |
| Reporting | SalesReport, CommissionReport | Read (view all financial reports; export settlement data) |

**Permission scope (Discovery Block 5):** View all financial reports; export settlement data.

---

### Marketing

| Bounded Context | Aggregates | Operation |
|---|---|---|
| Seating / Inventory | Batch | Read (view Batch performance) |
| Orders | Order | Read (view audience and conversion analytics) |
| Reporting | SalesReport | Read (view buyer/event analytics; export audience segments) |
| Event Management | Event | Read (view event analytics) |
| Seating / Inventory | — (Promotion) | Write (manage Promotions — create, activate, deactivate) |

> **Note:** Promotion is defined as a child concept of Seating/Inventory (Batches) and Orders in Phase 1. Marketing can manage Promotions but does not own Order or Ticket data.

**Permission scope (Discovery Block 5):** View buyer/event analytics; manage Promotions; view campaign performance; export audience segments.

---

### Platform Operator

| Bounded Context | Aggregates | Operation |
|---|---|---|
| Identity | User | Read (view all Users across platform) |
| Event Management | Event, Date | Write (approve or reject Event submissions; publish Events) |
| Orders | Order | Write (override Reservations in incident handling) |
| Payments | Payment, Payout | Read (view all platform Payments and Payouts) |
| Reporting | SalesReport, CommissionReport | Read (view all platform reports) |

**Permission scope (Discovery Block 5):** View all platform reports; configure platform settings; approve or reject Events before publication.

---

### Super Admin

| Bounded Context | Aggregates | Operation |
|---|---|---|
| Identity | User, Role | Read + Write (all Users, all Roles, all permissions across all Organizers) |
| Event Management | Event, Date | Read + Write (all Events, all Dates, all Organizers) |
| Seating / Inventory | Seat, Reservation, Batch | Read + Write (full inventory control) |
| Orders | Order, LineItem | Read + Write (all Orders) |
| Payments | Payment, Payout | Read + Write (all Payments, all Payouts) |
| Ticket Issuance | Ticket | Read + Write (all Tickets, including cancelation and invalidation) |
| Access Control | ValidationRecord | Read + Write (all ValidationRecords, all sync states) |
| Notifications | Notification | Read + Write (all Notifications) |
| Reporting | SalesReport, CommissionReport | Read (all reports, all Organizers) |

**Permission scope (Discovery Block 5):** Full platform control — all modules, all Organizers, all data.

---

## Summary matrix

| Actor | Identity | Event Mgt | Seating/Inventory | Orders | Payments | Ticket Issuance | Access Control | Notifications | Reporting |
|---|---|---|---|---|---|---|---|---|---|
| Buyer | R/W (self) | R | R/W | R/W | R/W | R | — | R | — |
| Organizer | R/W (scoped) | R/W (own) | R/W (own) | R (own) | R (own) | R (own) | — | R (own) | R (own) |
| Door Validator | — | — | — | — | — | R | W | — | — |
| Venue Staff | R (scoped) | — | — | — | — | — | R | — | — |
| Support | R | — | — | R | — | R/W | — | R/W | R |
| Finance | — | — | — | — | R | — | — | — | R |
| Marketing | — | R | R/W (Promotions) | R | — | — | — | — | R |
| Platform Operator | R | R/W | — | W | R | — | — | — | R |
| Super Admin | R/W | R/W | R/W | R/W | R/W | R/W | R/W | R/W | R |
