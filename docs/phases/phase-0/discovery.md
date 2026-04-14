# OrionTicket — Discovery Document
> **Client:** OrionTicket  
> **Vendor:** DevTodo un Poco  
> **Phase:** 0 — Discovery (Complete)  
> **Status:** ✅ All blockers resolved — Ready for Phase 1 (Domain Modeling)

---

## Product definition

OrionTicket is a white-label, multi-tenant ticket sales platform for massive events, with strong concurrency control and anti-fraud measures. Organizers operate their own branded space within the platform. The platform charges organizers a commission per transaction. The initial market is Guatemala.

---

## Critical constraint acknowledged

> **"Microservices" is an architectural constraint, not a product definition.**

The client expects independently deployed microservices from day 1. This constraint is accepted but carries significant risk given the budget ($50,000) and deadline (3 months) with a team of 2 backend developers. This tension must be explicitly managed in the roadmap by defining a strict MVP cut and deferring non-critical services to later phases.

---

## Block 1 — Vision, product, and business model

**1. What exactly is OrionTicket?**  
White-label solution for multiple organizers with a marketplace model. Each organizer manages their own events under the platform umbrella.

**2. Who pays to use the platform?**  
The organizer pays via a commission percentage retained per transaction by the platform.

**3. What is the main business objective in the first stage?**  
Validate the market and build scalable infrastructure from the start. These are dual goals that must be balanced carefully in the MVP scope.

**4. What concrete problem does the client want to solve?**  
Manage real concurrency in ticket sales with a resilient system. Current platforms allow sales to resellers, generating fraud and access control problems at the door.

**5. What will be the competitive differentiator?**  
Lower transaction cost for organizers and more operational control compared to existing alternatives.

**6. Definition of project success:**
- **3 months:** First functional version deployed and operational.
- **12 months:** ROI begins to be noticeable.
- **3 years:** Solid and recognized client base in the Guatemalan market.

---

## Block 2 — Market, scope, and multi-tenancy

**1. Initial operating region:** Guatemala only.  
*Implications: single currency (GTQ), single timezone (CST, UTC-6), local payment gateways, local tax regulations.*

**2. Number of organizers from v1:** 1 organizer.  
*System must be designed as multi-tenant from the start, but launched with a single tenant.*

**3. Organizer isolation:** Each organizer has their own panel, catalog, branding, rules, and reports.

**4. Multiple brands or domains per organizer:** No.

**5. Event origin:** Only own events. No third-party event sales.

**6. Platform configurability per organizer:** Low. The client wants a ready-to-use platform, not a highly configurable engine.

---

## Block 3 — Event and ticket domain

**1. Supported event types:** All types — concerts, theater, sports, conferences, festivals, recurring events, VIP experiences, seatless events, hybrid events.

**2. Multiple dates or performances per event:** Yes. Entity hierarchy: Event → Session/Performance → Venue → Availability.

**3. Seating model:** Both. Seating map (assigned) and general admission (unassigned) events must be supported.

**4. Seat selection:** User chooses the exact seat. No automatic or manual assignment by staff.

**5. Venue structure:** Zones, sections, rows, numbered seats, tables, boxes, and packages are all supported.

**6. Ticket types:** Individual tickets only. No combos, VIP packages, add-ons, or merchandising in v1.

**7. Ticket attributes:** Type, price, currency, zone, benefits, restrictions, QR, status, holder, transferability flag, access policy, batch, associated promotion.

**8. Ticket state machine:**
```
reserved → pending_payment → paid → issued → used
                                  → canceled
                                  → invalidated
```
> **v1 decision:** No refunds, no transfers. Once issued, a ticket can only be used, canceled (by platform/organizer), or invalidated.

**9. Sales phases and batches:** Presales, sales phases, batches, quotas, promotional codes, and restricted access are all supported.

**10. Resale and transfer — v1 decision:** ✅ **No resale. No transfer.** A ticket belongs to the original buyer until the event. This is a firm v1 policy. Resale/transfer may be evaluated for v2.

---

## Block 4 — Purchase flow and end-user experience

**1. Event discovery:** Main landing page, search engine, filters (category, city, date, organizer), and external campaign links.

**2. Registration requirement:** Users must register before purchasing. Guest checkout is not supported.

**3. Cart model:** One purchase per event. Multi-event cart is not supported in v1.

**4. Seat reservation hold duration:** 10 minutes. After expiration, the hold is released and the seat returns to inventory.

**5. Concurrency conflict resolution:** First to complete payment wins the seat. A user who holds a seat but does not pay within 10 minutes loses it.

**6. Checkout flow:**
```
Seat selection → Reservation (hold) → Identity confirmation → Payment → Confirmation → Ticket issuance → Delivery
```

**7. Ticket delivery channels:** Email, digital wallet, dynamic QR, PDF, mobile app download, direct download.

**8. Buyer portal:** Yes. Buyers can view purchases, download tickets, and contact support. Transfer and refund options are not available in v1.

**9. Mobile experience:** Responsive web only. No native app in v1.

---

## Block 5 — Internal operation, panels, and backoffice

**1. Internal roles and permissions:**

| Role | Permissions |
|---|---|
| **Super Admin** | Full platform control — all modules, all organizers, all data |
| **Platform Operator** | View all platform reports, configure platform settings |
| **Organizer** | Create & manage own events, configure venue & seating maps, set prices/batches/promotions, view own sales reports, manage own staff, manage own buyers |
| **Venue Staff** | Coordinate door validators, view access/validation reports |
| **Door Validator** | Scan & validate QR at entry only |
| **Support** | View & respond to buyer tickets, resend tickets manually, view order details, escalate to operator |
| **Finance** | View all financial reports, export settlement data |
| **Marketing** | View buyer/event analytics, manage promotional codes, view campaign performance, export audience segments |

**2. Mandatory panels in v1:**
- Organizer administration panel (scoped to own data).
- Super admin panel (full platform visibility).

**3. Event publication workflow:** Organizers submit events. Platform Operator (or Super Admin) approves or rejects before publication. No self-publishing.

**4. Backoffice management scope:** Events, venues, seating maps, prices, batches, promotions, orders, users, accesses, staff, refunds (platform-level cancellations), reports, auditing.

**5. Check-in / door validation:** Yes. Required from v1.

**6. Validation modes:** Real-time (primary) and offline with sync tolerance (required for venues with intermittent connectivity).

**7. Operational incident handling:** Yes. Support can resend tickets, view orders, and escalate. Platform Operator can override reservations. No self-service refunds (v1 has no refunds).

**8. CRM / marketing automation:** No. Not in scope.

---

## Block 6 — Money, payments, refunds, and reconciliation

**1. Supported payment methods in v1:** Credit/debit cards and bank transfers.

**2. Who processes the charge:** The platform.

**3. Revenue distribution:** The platform retains a percentage commission per sale. The remainder is settled to the organizer.

**4. Automatic reconciliation:** Yes. Required from v1.

**5. Refund policy — v1 decision:** ✅ **No refunds.** All sales are final. This is a firm v1 policy. Refund support may be introduced in v2 if the business model requires it.

**6. Tax receipts / invoicing:** No. Not in scope for v1.

**7. Variable commission rules:** No. A single platform-wide commission rate applies.

**8. Mandatory financial reports:** Sales reports, commission reports, refund reports (for v2 reference), chargeback reports.

---

## Block 7 — Risk, fraud, security, and compliance

**1. Expected fraud profile:** Medium. Priority threats: bots, mass purchases by a single actor, unauthorized resale.

**2. Anti-fraud mechanisms required:** Virtual queue for high-demand events, rate limiting per user/IP, anti-bot protection (CAPTCHA or equivalent).

**3. Sensitive data stored:** Personal user data (PII), payment data (must not store raw card data — PCI scope), event data, ticket data.

**4. Legal and regulatory requirements:** Data protection (Guatemalan law), sales terms, return policies (even if no refunds, terms must be explicit), tax regulations, marketing consent.

**5. Full audit trail:** Yes. All sensitive changes and actions must be traceable with actor, timestamp, and previous state.

**6. Unacceptable incidents (zero-tolerance):**
- Seat overbooking
- Double ticket issuance for the same seat
- System crash at event launch / presale spike
- Duplicate QR validation at the door (same ticket accepted twice)
- Lost or unrecoverable orders

---

## Block 8 — Scalability, availability, and non-functional requirements

**1. Expected load:**

| Condition | Concurrent users | Transactions/min |
|---|---|---|
| Normal | 5,000 | 100 |
| Peak | 30,000 | 1,000 |

**2. Worst expected peak:** Major artist presale or sports final. System must sustain 30,000 concurrent users without degradation.

**3. SLA:** 99.9% availability (~8.7 hours downtime/year maximum).

**4. Latency targets:**

| Operation | Max latency |
|---|---|
| Purchase / checkout | 1,000 ms |
| Ticket issuance | 500 ms |
| QR validation at door | 100 ms |

**5. Maximum tolerable downtime:** 1 hour. RTO ≤ 1h, RPO must be defined in architecture phase.

**6. Consistency model:**

| Data | Consistency requirement |
|---|---|
| Ticket inventory | Strong / real-time |
| Order status | Strong / real-time |
| Payment confirmation | Strong / real-time |
| Access validations | Strong / real-time |
| Reports and metrics | Eventual |
| Notification logs | Eventual |
| Audit logs | Eventual (append-only) |

**7. Observability:** Required from day 1. Includes structured logging, distributed tracing, business metrics, alerting, and operational dashboards. This is non-optional in a distributed system.

---

## Block 9 — Integrations and ecosystem

**1. Required integrations in v1:**
- Payment gateways (cards and transfers, Guatemala-compatible).
- Email / SMS / WhatsApp for notifications.
- Analytics platform.

**2. Public / partner API:** Yes. Versioned external API required. Scope and consumers to be defined in architecture phase.

**3. External seating software:** No. Seating maps will be built and managed natively.

**4. Data migration:** No. Greenfield system.

**5. External sales channels:** No. Web platform only in v1. No kiosks, call centers, or third-party apps.

---

## Block 10 — Project, team, and implementation constraints

**1. What "microservices" means to the client:** Scalability and resilience expectation. The client expects the system to handle peak loads and recover from failures independently per service.

**2. Deployment expectation:** Independently deployed microservices from day 1.

**3. Team composition:**

| Role | Count |
|---|---|
| Backend developer | 2 |
| Frontend developer | 1 |
| DevOps | 1 |
| QA | 1 |
| Security | 1 |
| Observability | 1 |
| Data | 1 |
| Support | 1 |

**4. Production operator:** The development team.

**5. Budget and deadline:** $50,000 USD / 3 months.

**6. Risk tolerance:** Moderate.

**7. Priority:** Strong technical foundation for the long term over speed-to-market.

**8. Mandatory in v1:**
- Event management
- Ticket sales (with seating)
- Ticket validation (check-in)
- Client / buyer management
- Notifications
- Payments
- Reports

**9. Desirable but deferred to v2:**
- Custom organizer panel (beyond basic)
- Social media integrations
- Additional payment gateway integrations
- Billing system integrations
- Partner API integrations
- Refund support
- Resale / transfer support

---

## Block 11 — Architectural decisions (resolved)

**1. Bounded contexts and service candidates:**

| Bounded Context | Owns |
|---|---|
| Identity | Users, credentials, sessions, roles, permissions |
| Catalog | Event definitions, venue metadata, public-facing listings |
| Event Management | Event lifecycle, approval workflow, seating map configuration |
| Seating / Inventory | Seat state, holds, availability, expiration jobs |
| Pricing | Price tiers, batches, promotional codes, quota rules |
| Orders | Checkout state, order lifecycle, idempotency |
| Payments | Payment transactions, gateway integration, settlements, commissions |
| Ticket Issuance | QR generation, ticket records, delivery coordination |
| Access Control | Check-in records, validation state, offline sync |
| Notifications | Event-driven message delivery (email/SMS/WhatsApp) |
| Reporting | Aggregated metrics, exports, financial reports |

**2. Data ownership:** Each bounded context owns its data exclusively. No shared databases between services. Reporting reads via events or projections — it does not query other services' databases directly.

**3. Consistency boundaries:** Inventory, Orders, Payments, Issuance, and Access Control require strong consistency. All others tolerate eventual consistency.

**4. Idempotency strategy:** Idempotency keys composed from: transaction ID + buyer ID + event ID + seat ID + timestamp (to millisecond precision). Applied at Orders, Payments, and Issuance boundaries.

**5. Domain events catalog:**

| Event | Producer | Consumers |
|---|---|---|
| EventPublished | Event Management | Catalog, Reporting |
| SeatHeld | Seating/Inventory | Orders |
| HoldExpired | Seating/Inventory | Orders, Notifications |
| OrderCreated | Orders | Payments, Notifications |
| PaymentAuthorized | Payments | Orders, Ticket Issuance |
| PaymentFailed | Payments | Orders, Notifications, Inventory |
| TicketIssued | Ticket Issuance | Notifications, Reporting |
| TicketCheckedIn | Access Control | Reporting, Notifications |
| TicketInvalidated | Ticket Issuance | Access Control, Reporting |

**6. Sync vs async communication — resolved:**

**Synchronous (request/response):**
- Seat selection → Inventory hold (must be immediate, strong consistency)
- QR scan → Access Control validation (100ms SLA, cannot tolerate async)
- Payment gateway webhook → Orders status update (atomic confirmation)

**Asynchronous (event-driven via message broker):**
- OrderCreated → Payments begins processing
- PaymentAuthorized → Ticket Issuance generates QR
- TicketIssued → Notifications sends delivery
- HoldExpired → Inventory releases seat
- PaymentFailed → Inventory releases seat + Notifications alerts buyer
- EventPublished → Reporting indexes event

**7. High-consistency surfaces under concurrency:** Seating/Inventory and Orders are the primary contention points during peak sales. These require optimistic or pessimistic locking strategies and must be designed first.

**8. Observability strategy:** Structured logs (JSON), distributed tracing (OpenTelemetry), business metrics per service, alerting on SLA breaches, and a central operational dashboard. Each service emits traces from day 1. This is infrastructure, not a feature.

**9. API versioning:** Semantic versioning (v1, v2...) on all public-facing and inter-service contracts. Breaking changes require a new version. Old versions supported for a defined deprecation window.

**10. Core vs cross-cutting boundary:**
- **Core business services:** Catalog, Event Management, Seating/Inventory, Pricing, Orders, Payments, Ticket Issuance, Access Control.
- **Cross-cutting support services:** Identity, Notifications, Reporting, Observability infrastructure (not a microservice — shared concern).

---

## Block 12 — Final synthesis

**1. Product definition in one sentence:**  
OrionTicket is a white-label, multi-tenant ticket sales platform for Guatemala that provides organizers with full control over their events, eliminates resale fraud through strict ownership policies, and handles massive concurrency safely.

**2. Primary and secondary users:**

| Type | Users |
|---|---|
| Primary | Buyers (end users purchasing tickets) |
| Primary | Organizers (managing events and operations) |
| Secondary | Super Admin, Platform Operator, Finance, Marketing, Support |
| Operational | Venue Staff, Door Validators |

**3. Business and monetization model:**  
B2B2C. Organizers pay the platform a commission percentage per ticket sold. The platform processes payments centrally and settles the remainder to the organizer. No refunds in v1. No variable commissions.

**4. Exact MVP and explicit exclusions:**

| In MVP (v1) | Explicitly excluded from v1 |
|---|---|
| Event creation and approval workflow | Refunds |
| Seating map configuration | Ticket transfer or resale |
| Ticket sales with hold and expiration | Multi-event cart |
| Payment processing (card + transfer) | Native mobile app |
| Ticket issuance (QR, email, PDF) | CRM / marketing automation |
| Real-time and offline check-in | Third-party event sales |
| Buyer portal | External sales channels |
| Organizer and super admin panels | Billing / invoicing |
| Notifications (email/SMS/WhatsApp) | Advanced partner API |
| Financial reports and reconciliation | Social media integrations |
| Anti-fraud (rate limiting, queue, anti-bot) | |
| Observability from day 1 | |

**5. Critical business flows:**
1. Organizer creates event → Platform Operator approves → Event published.
2. Buyer discovers event → Selects seat → Hold created (10 min) → Pays → Ticket issued → Delivered.
3. Hold expires without payment → Seat released → Inventory updated.
4. Door Validator scans QR → Real-time validation → Check-in recorded.
5. Offline validator syncs → Queued validations reconciled → Duplicates rejected.

**6. Main operational and technical risks:**

| Risk | Severity | Mitigation |
|---|---|---|
| $50K / 3 months for full microservices | Critical | Strict MVP cut, defer non-core services, modular internal structure first |
| 2 backend devs for 10+ services | High | Prioritize 4–5 core services in v1, stub the rest |
| Seat contention under 30K concurrent users | High | Pessimistic locking or distributed lock on inventory, load test before launch |
| Observability gaps in distributed system | High | OpenTelemetry from service 1, non-negotiable |
| Offline check-in sync conflicts | Medium | Conflict resolution strategy defined before Access Control is built |
| No refund policy — buyer disputes | Medium | Clear terms of service, explicit acceptance at checkout |
| Single team operating production | Medium | Runbooks, automated alerting, on-call rotation defined pre-launch |

**7. Priority non-functional requirements:**
1. Inventory strong consistency under peak concurrency.
2. Validation latency ≤ 100ms.
3. 99.9% availability.
4. Zero overbooking, zero double issuance.
5. Full audit trail on all sensitive operations.
6. Observability from day 1.
7. Anti-fraud perimeter (rate limiting, virtual queue, anti-bot).

**8. Bounded contexts and microservice candidates:** See Block 11, section 1.

**9. Mandatory integrations:**
- Payment gateway (Guatemala — cards and transfers).
- Email provider.
- SMS / WhatsApp provider.
- Analytics platform.

**10. Phased roadmap:**

| Phase | Scope | Target |
|---|---|---|
| **Sprint 1–2** | Identity, Event Management (basic), Catalog, Seating/Inventory core | Internal — no buyer-facing yet |
| **Sprint 3–4** | Orders, Payments (gateway integration), Ticket Issuance, Notifications | First end-to-end purchase flow |
| **Sprint 5–6** | Access Control (real-time + offline), Pricing (batches, promos), Buyer portal | Operational readiness |
| **Sprint 7–8** | Reporting, Financial reconciliation, Organizer panel, Super admin panel | Full v1 |
| **Post-launch** | Partner API, Refunds (v2), Transfer/resale (v2), Native app (v2) | v2 roadmap |

---

## Recommended next steps (Phase 1 — Domain Modeling)

1. **Event storming session** — walk through all domain events with the full team to validate the event catalog and detect missing flows.
2. **Ubiquitous language glossary** — define terms that must be consistent across code, docs, and conversation (e.g., "hold" vs "reservation" vs "seat lock").
3. **RBAC matrix** — formalize the role/permission table into an authorization model consumable by the Identity service.
4. **Seating map domain model** — this is the most complex domain object. Model it before anything else is built.
5. **Architecture Decision Records (ADRs)** — document the key decisions made in this discovery (no refunds v1, no transfers v1, sync/async boundaries, data ownership) so they are traceable.
6. **Load test baseline** — define the concurrency test scenario for 30K users before a single line of production code is written.
