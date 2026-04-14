# Business Rules

> **Phase:** 2 — Use Cases & Flows  
> **Source:** Extracted exclusively from `docs/phases/phase-0/discovery.md` and `docs/phases/phase-1/*.md`.  
> **Constraint:** Every rule references the artifact and block/section it originates from. Only ubiquitous language from `docs/phases/phase-1/ubiquitous-language-glossary.md` is used.

---

## Identity

| # | Rule | Source |
|---|---|---|
| BR-ID-01 | A User must register before purchasing a Ticket. Guest checkout is not supported. | discovery.md — Block 4.2 |
| BR-ID-02 | Each User has exactly one Role. A Role owns a set of permissions. | aggregate-definitions.md — Identity |
| BR-ID-03 | A User scoped to an Organizer (e.g., Door Validator, Venue Staff) can only act within that Organizer's data. | discovery.md — Block 5.1 |
| BR-ID-04 | A User status must be ACTIVE to perform any operation. SUSPENDED and UNVERIFIED Users are blocked. | aggregate-definitions.md — Identity |

---

## Event Management

| # | Rule | Source |
|---|---|---|
| BR-EM-01 | An Organizer may only create and manage their own Events. No Organizer may access another Organizer's Events. | discovery.md — Block 2.3 |
| BR-EM-02 | An Event must be submitted for review by the Organizer and approved by a Platform Operator or Super Admin before it is Released (visible to Buyers). Self-publishing is not allowed. | discovery.md — Block 5.3 |
| BR-EM-03 | An Event has one or more Dates. Each Date is a child of an Event. | discovery.md — Block 3.2; aggregate-definitions.md — Event Management |
| BR-EM-04 | An Event may be canceled by the Organizer or by a Platform Operator / Super Admin at any lifecycle stage. | domain-events-map.md — Event; discovery.md — Block 5.1 |
| BR-EM-05 | A Venue supports Zones, Sections, Rows, numbered Seats, tables, boxes, and packages. The Seating Map structure is configured per Venue. | discovery.md — Block 3.5 |
| BR-EM-06 | Seating Maps are built and managed natively within the platform. No external seating software is integrated. | discovery.md — Block 9.3 |
| BR-EM-07 | Only own Events are sold on the platform. Third-party event sales are not supported in v1. | discovery.md — Block 2.5 |

---

## Seating / Inventory

| # | Rule | Source |
|---|---|---|
| BR-SI-01 | Capacity (total Seats or Tickets for an Event/Date) is fixed at Event/Date creation and never changes during sale. | ubiquitous-language-glossary.md — term 12 |
| BR-SI-02 | Availability (remaining Tickets) is calculated dynamically and never persisted as a stored value. | ubiquitous-language-glossary.md — term 13 |
| BR-SI-03 | A Seat may be of type MAPPED (assigned seat) or GENERAL_ADMISSION (unassigned). Both behaviors are unified under the Seat aggregate. | aggregate-definitions.md — Seating/Inventory |
| BR-SI-04 | A Reservation locks a Seat for exactly 10 minutes. After expiration the Reservation status becomes EXPIRED and the Seat returns to AVAILABLE. | discovery.md — Block 4.4; aggregate-definitions.md — Seating/Inventory |
| BR-SI-05 | The first Buyer to complete payment wins the Seat. A Buyer who holds a Reservation but does not pay within 10 minutes loses it. | discovery.md — Block 4.5 |
| BR-SI-06 | A Batch is a time-limited group of Tickets at a specific price with a fixed Capacity quota. The Batch sold count is incremented atomically with the Reservation to prevent overselling. | aggregate-definitions.md — Seating/Inventory |
| BR-SI-07 | A Batch may be in status SCHEDULED, ACTIVE, EXHAUSTED, or EXPIRED. A Buyer can only purchase from an ACTIVE Batch. | aggregate-definitions.md — Seating/Inventory; domain-events-map.md — Pricing & Batches |
| BR-SI-08 | Seat overbooking is a zero-tolerance incident. The system must guarantee that no Seat is sold more than once. | discovery.md — Block 7.6 |

---

## Orders

| # | Rule | Source |
|---|---|---|
| BR-OR-01 | One Order covers one Event only. Multi-event Orders (cart) are not supported in v1. | discovery.md — Block 4.3 |
| BR-OR-02 | An Order is created when the Buyer proceeds to payment after holding a Reservation. | aggregate-definitions.md — Orders; ubiquitous-language-glossary.md — term 4 |
| BR-OR-03 | An Order holds a reference to the Reservation. If the Reservation expires, the Order expires and moves to status EXPIRED. | aggregate-definitions.md — Orders |
| BR-OR-04 | A Promotion discount is applied at the Order level. Only one Promotion per Order. | aggregate-definitions.md — Orders |
| BR-OR-05 | The Service Fee is calculated and included in the Order total before payment is initiated. | aggregate-definitions.md — Orders; ubiquitous-language-glossary.md — term 17 |
| BR-OR-06 | Idempotency for Order creation is enforced via a key composed of: transaction ID + Buyer ID + Event ID + Seat ID + timestamp to millisecond precision. | discovery.md — Block 11.4 |
| BR-OR-07 | A Platform Operator may override (release) a Reservation in incident handling scenarios. | discovery.md — Block 5.7 |

---

## Payments

| # | Rule | Source |
|---|---|---|
| BR-PA-01 | The platform processes all payments centrally. No direct payment between Buyer and Organizer. | discovery.md — Block 6.2 |
| BR-PA-02 | Supported payment methods in v1 are credit/debit cards and bank transfers only. | discovery.md — Block 6.1 |
| BR-PA-03 | All sales are final. No refunds are supported in v1. | discovery.md — Block 6.5 |
| BR-PA-04 | The platform retains a single, platform-wide commission (Service Fee) per transaction. No variable commission rules exist. | discovery.md — Block 6.7; ubiquitous-language-glossary.md — term 17 |
| BR-PA-05 | The remainder after the Service Fee is settled to the Organizer as a Payout. | discovery.md — Block 6.3; ubiquitous-language-glossary.md — term 18 |
| BR-PA-06 | A Payout has a maximum of 1 automatic retry on failure. | aggregate-definitions.md — Payments |
| BR-PA-07 | Automatic financial reconciliation is required from v1. | discovery.md — Block 6.4 |
| BR-PA-08 | Raw card data must not be stored. PCI scope must be respected. | discovery.md — Block 7.3 |
| BR-PA-09 | Idempotency for Payment is enforced via the same key schema as Orders (transaction ID + Buyer ID + Event ID + Seat ID + timestamp). | discovery.md — Block 11.4 |
| BR-PA-10 | If a Payment fails, the Seat Reservation is released and the Buyer is notified. | discovery.md — Block 11.6 |

---

## Ticket Issuance

| # | Rule | Source |
|---|---|---|
| BR-TI-01 | A Ticket is issued only after PaymentAuthorized. No Ticket is issued before payment confirmation. | aggregate-definitions.md — Ticket Issuance; domain-events-map.md — Purchase Flow |
| BR-TI-02 | Double Ticket issuance for the same Seat is a zero-tolerance incident. | discovery.md — Block 7.6 |
| BR-TI-03 | A Ticket QR code is dynamic and regenerates every 2 minutes (TTL controlled). | aggregate-definitions.md — Ticket Issuance |
| BR-TI-04 | Ticket delivery channels supported in v1: Email, digital wallet, dynamic QR, PDF, mobile download, direct download. | discovery.md — Block 4.7 |
| BR-TI-05 | Once issued, a Ticket can only transition to: USED (at check-in), CANCELED (by platform or Organizer), or INVALIDATED (fraud detection). No transfer or resale. | discovery.md — Block 3.8; Block 3.10 |
| BR-TI-06 | Idempotency for Ticket issuance is enforced via the same key schema as Orders and Payments. | discovery.md — Block 11.4 |
| BR-TI-07 | A Ticket for a MAPPED Seat carries a seatId. A Ticket for GENERAL_ADMISSION has seatId = null. | aggregate-definitions.md — Ticket Issuance |
| BR-TI-08 | A Support user may manually trigger Ticket resend. This creates a new delivery action but does not change Ticket status. | discovery.md — Block 5.7; Block 5.1 |

---

## Access Control

| # | Rule | Source |
|---|---|---|
| BR-AC-01 | QR Validation at the door must complete within 100 ms. | discovery.md — Block 8.4 |
| BR-AC-02 | Both real-time Validation (primary) and offline Validation with sync tolerance are required from v1. | discovery.md — Block 5.6 |
| BR-AC-03 | First-scan-wins: if a Ticket is scanned twice (duplicate QR Validation), the second scan is rejected. Duplicate QR Validation is a zero-tolerance incident. | discovery.md — Block 7.6; aggregate-definitions.md — Access Control |
| BR-AC-04 | A ValidationRecord is immutable once written. It is never modified, only appended. | aggregate-definitions.md — Access Control |
| BR-AC-05 | A Validation that fails records a failureReason: ALREADY_USED, WRONG_EVENT, EXPIRED, or INVALIDATED. | aggregate-definitions.md — Access Control; domain-events-map.md — Access Control |
| BR-AC-06 | Offline Validation: scans queued while offline must be reconciled upon sync. Conflicts detected during sync trigger ConflictDetected and are rejected, never silently accepted. | discovery.md — Block 5.6; domain-events-map.md — Access Control |
| BR-AC-07 | Access Control records the result of a Validation attempt but does not modify the Ticket aggregate. If fraud is detected, it fires TicketInvalidated to Ticket Issuance. | aggregate-definitions.md — Access Control |

---

## Notifications

| # | Rule | Source |
|---|---|---|
| BR-NO-01 | Notifications are event-driven. Notification dispatch is triggered by domain events (e.g., TicketIssued, HoldExpired, PaymentFailed). | discovery.md — Block 11.6; domain-events-map.md — Notifications |
| BR-NO-02 | Supported Notification channels in v1: Email, SMS, WhatsApp. | discovery.md — Block 9.1 |
| BR-NO-03 | A Notification has a retryCount. Support may trigger a manual resend. | aggregate-definitions.md — Notifications |

---

## Reporting

| # | Rule | Source |
|---|---|---|
| BR-RE-01 | Reporting only reads data via domain events or projections. It does not query other services' databases directly. | discovery.md — Block 11.2 |
| BR-RE-02 | Report data is eventually consistent. Strong consistency is not required. | discovery.md — Block 8.6 |
| BR-RE-03 | Mandatory reports in v1: Sales reports, Commission reports, Chargeback reports. | discovery.md — Block 6.8 |
| BR-RE-04 | Organizer may only view reports scoped to their own Events and data. | discovery.md — Block 5.1 |

---

## Cross-cutting rules

| # | Rule | Source |
|---|---|---|
| BR-CC-01 | A full Audit Log is required for all sensitive changes. Each entry records: actor, role, action, targetEntity, targetId, previousState, newState, timestamp. | discovery.md — Block 7.5; aggregate-definitions.md — Cross-cutting |
| BR-CC-02 | The Audit Log is append-only. No entry is ever modified or deleted. | aggregate-definitions.md — AuditLog |
| BR-CC-03 | Anti-fraud measures required from v1: rate limiting per User/IP, anti-bot protection (CAPTCHA or equivalent), virtual queue for high-demand Events. | discovery.md — Block 7.2 |
| BR-CC-04 | The system must sustain 30,000 concurrent Users without degradation at peak load. | discovery.md — Block 8.2 |
| BR-CC-05 | SLA: 99.9% availability. Maximum tolerable downtime: 1 hour (RTO ≤ 1h). | discovery.md — Block 8.3, 8.5 |
| BR-CC-06 | Observability (structured logging, distributed tracing, business metrics, alerting) is required from day 1 and from service 1. | discovery.md — Block 8.7; Block 11.8 |
| BR-CC-07 | All inter-service and public-facing API contracts are semantically versioned (v1, v2...). Breaking changes require a new version. | discovery.md — Block 11.9 |
| BR-CC-08 | Each Bounded Context owns its data exclusively. No shared databases between services. | discovery.md — Block 11.2 |
| BR-CC-09 | Inventory, Orders, Payments, Ticket Issuance, and Access Control require strong / real-time consistency. All others tolerate eventual consistency. | discovery.md — Block 8.6; Block 11.3 |
| BR-CC-10 | The platform operates only in Guatemala (v1): single currency (GTQ), single timezone (CST, UTC-6). | discovery.md — Block 2.1 |
