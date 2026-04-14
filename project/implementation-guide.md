# Implementation Guide — 45-Day Strategy

> **Project:** OrionTicket  
> **Duration:** 45 working days  
> **Team:** 4 developers  
> **Structure:** 1 setup week + 4 sprints  
> **Stack:** Java 21 + Spring Boot 3, Angular 17+, Docker Compose, RabbitMQ, Spring Cloud Gateway, PostgreSQL, VPS  
> **Service ownership:** See [project/TEAM.md](TEAM.md)

---

## Timeline Overview

| Period | Days | Focus |
|---|---|---|
| **Setup Week** | Days 1–7 | All 4 people together. Infrastructure, contracts, mocks, dev environment. |
| **Sprint 1** | Days 8–17 | Foundation: Identity, Event Management, basic Organizer Panel |
| **Sprint 2** | Days 18–27 | Core transaction: Seating/Inventory, Orders, Payments, Checkout Flow |
| **Sprint 3** | Days 28–37 | Ticket lifecycle: Ticket Issuance, Notifications, Buyer Portal |
| **Sprint 4** | Days 38–45 | Operations: Access Control, Reporting, Validator App, Stabilization |

---

## Setup Week — Days 1–7 (All 4 Together)

> **Goal:** Before anyone writes production code, the entire infrastructure foundation must exist and every developer must be able to run the full system locally with one command.

### Day 1 — Repository and tooling

| Who | Task |
|---|---|
| All | Clone repo. Read INDEX.md, TEAM.md, and Definition of Done. |
| Person 1 | Create root `docker-compose.yml` with: PostgreSQL (9 databases, one per service), RabbitMQ (management UI on :15672), Spring Cloud Gateway (port 8080). |
| Person 2 | Create `Makefile` with targets: `make up`, `make down`, `make logs`, `make restart {service}`. |
| Person 3 | Set up GitHub Projects board with 4 sprint columns. Populate from product-backlog.md. |
| Person 4 | Set up GitHub branch protection rules on `main` (2 approvals) and `develop` (1 approval, CI required). |

### Day 2 — Service scaffolding

| Who | Task |
|---|---|
| Person 1 | Scaffold `identity-service` and `event-management-service` (Spring Boot 3, Maven, spring-security, spring-amqp, springdoc-openapi, flyway, testcontainers). |
| Person 2 | Scaffold `seating-inventory-service`. |
| Person 3 | Scaffold `orders-service` and `payments-service`. |
| Person 4 | Scaffold `ticket-issuance-service`, `access-control-service`, `notifications-service`, `reporting-service`. |
| All | Each service: Dockerfile (multi-stage), `application.yml` with env vars, health check endpoint, Flyway baseline migration, added to `docker-compose.yml`. |

### Day 3 — Database migrations and gateway routes

| Who | Task |
|---|---|
| All | Write Flyway V1 migration for each owned service using ER diagrams from `docs/phases/phase-3/er-diagrams/`. |
| Person 1 | Configure Spring Cloud Gateway routes for all 9 services. JWT validation filter configured (accepts any valid JWT for now). |
| All | Verify: `docker-compose up` starts all containers, all health checks pass, gateway routes to each service. |

### Day 4 — RabbitMQ and event infrastructure

| Who | Task |
|---|---|
| Person 1 | Create shared RabbitMQ configuration: exchanges (topic), queues per consumer, dead-letter exchanges and dead-letter queues. Publish as `rabbitmq/definitions.json` loaded on container start. |
| All | Each service: implement base `DomainEventPublisher` and `DomainEventConsumer` classes using Spring AMQP. Publish a test event, verify it arrives in RabbitMQ management UI. |
| All | Verify: each service can publish and consume at least one test event. |

### Day 5 — Mocks and contract verification

| Who | Task |
|---|---|
| Person 1 | Publish mock Identity auth: a shared test JWT generator and a stub `/v1/users/{userId}` endpoint that returns a hardcoded User. |
| Person 2 | Publish mock Seating/Inventory: stub `/v1/reservations` that returns a hardcoded Reservation. |
| Person 3 | Publish mock Orders: stub OrderCreated event publisher (test script that sends event to RabbitMQ). Publish mock PaymentAuthorized event publisher. |
| Person 4 | Publish mock Ticket Issuance: stub `/v1/tickets/{ticketId}` that returns a hardcoded Ticket for Access Control QR lookup. |
| All | Each downstream service verifies it can call its upstream mock and receive expected responses. |

### Day 6 — CI/CD and observability baseline

| Who | Task |
|---|---|
| Person 1 | GitHub Actions CI pipeline: on PR to `develop` → compile all services, run unit tests, build Docker images. |
| Person 4 | OpenTelemetry configuration: add `opentelemetry-javaagent` to each Dockerfile. Verify traces appear in logs with traceId and spanId. |
| All | Run full `docker-compose up`. Verify all 9 services start, health checks pass, gateway routes work, RabbitMQ management shows exchanges and queues. |

### Day 7 — Sprint 1 planning and final check

| Who | Task |
|---|---|
| All | Sprint 1 planning session (1 hour). Assign user stories US-001 through US-012. |
| All | Final verification: every developer can `git pull && docker-compose up` and have the full environment running. |
| All | Contract review: walk through `service-contracts.md` and `event-schemas.md` as a team. Flag any ambiguities. |

### Setup Week Exit Criteria

- [ ] `docker-compose up` starts all 9 services + PostgreSQL + RabbitMQ + Gateway.
- [ ] All health checks return `UP`.
- [ ] Gateway routes to all services (verified with curl).
- [ ] RabbitMQ management shows all exchanges and queues.
- [ ] Each service has at least one Flyway migration applied.
- [ ] Each service can publish and consume a test event.
- [ ] All upstream mocks available for downstream services.
- [ ] CI pipeline passes on a test PR.
- [ ] GitHub Projects board populated with all user stories.

---

## Sprint 1 — Days 8–17: Foundation

### Per-person deliverables

#### Person 1 — Identity + Event Management

| Day | Deliverable |
|---|---|
| 8–9 | US-001: POST `/v1/auth/register` live. User created with UNVERIFIED status. Duplicate email returns 409. Unit + integration tests passing. |
| 10 | US-002: POST `/v1/auth/login` returns JWT. Spring Security configured. Gateway validates JWT on all protected routes. |
| 11–12 | US-003: User/Role CRUD endpoints. AuditLog entries emitted. Super Admin authorization enforced. |
| 13 | US-004: POST `/v1/events` and POST `/v1/events/{eventId}/dates`. EventCreated and DateAdded published to RabbitMQ. |
| 14 | US-005: Venue creation endpoint. US-006: Event submission endpoint. EventSubmittedForReview published. |
| 15 | US-007: Event approval/rejection endpoints. EventReleased published. Only Platform Operator/Super Admin authorized. |
| 16 | US-010: Basic Organizer Panel (Angular): login, event list, create event form, submit for review button. |
| 17 | US-011: Basic Super Admin Panel (Angular): user list, role management, event approval queue. US-012: Public catalog endpoint. |

#### Person 2 — Seating/Inventory (Sprint 1: preparation)

| Day | Deliverable |
|---|---|
| 8–10 | US-013 prep: Seating Map data model implemented. Flyway migration final. POST `/v1/events/{eventId}/dates/{dateId}/seating-map` endpoint scaffolded with request validation. |
| 11–13 | US-013 complete: Seat records created for MAPPED and GENERAL_ADMISSION. SeatingMapConfigured event published. Integration tests with TestContainers. |
| 14–15 | US-014: Batch creation endpoint. BatchCreated/BatchScheduled events published. Scheduled job for BatchActivated at scheduledStartAt. |
| 16–17 | US-015: GET seat availability endpoint. Seat list with status, price, zone information. Public endpoint. Seating Map configuration UI in Organizer Panel. |

#### Person 3 — Orders + Payments (Sprint 1: preparation)

| Day | Deliverable |
|---|---|
| 8–10 | Order data model and Flyway migration. Promotion data model. POST `/v1/orders` endpoint scaffolded with request validation. Consumes ReservationCreated mock event. |
| 11–13 | Payment data model, Payout data model, Flyway migrations. POST `/v1/payments` endpoint scaffolded. Gateway integration research and SDK selection for Guatemala-compatible payment processor. |
| 14–17 | Price resolution logic implemented: Batch price + Promotion discount + Service Fee calculation. Idempotency key generation utility built (shared across Orders and Payments). Checkout flow UI scaffolded in Angular. |

#### Person 4 — Ticket Issuance + Access Control + Notifications + Reporting (Sprint 1: preparation)

| Day | Deliverable |
|---|---|
| 8–10 | Ticket data model and Flyway migration. QR code generation utility (dynamic, 2-min TTL). TicketIssued event schema implemented. Consumes PaymentAuthorized mock event. |
| 11–13 | Notification data model and Flyway migration. Email provider integration (e.g., SendGrid, AWS SES). NotificationDispatched/Delivered/Failed events. Dead-letter queue configured. |
| 14–15 | ValidationRecord data model and Flyway migration. POST `/v1/validations` endpoint scaffolded with mock Ticket lookup. |
| 16–17 | Reporting data model (SalesReport, CommissionReport, AccessReport projections). Event consumers scaffolded for TicketIssued, PaymentAuthorized, ValidationSucceeded. |

---

## Sprint 2 — Days 18–27: Core Transaction

### Per-person deliverables

#### Person 1 — Identity + Event Management (stabilization + support)

| Day | Deliverable |
|---|---|
| 18–19 | US-008: Event cancelation. EventCanceled and DateCanceled published. Cascading Reservation release via event. |
| 20–21 | US-009: Organizer staff management endpoint. |
| 22–27 | Support Person 2 and Person 3 on cross-service integration. Fix auth/gateway issues. Update mocks as real endpoints go live. Organizer Panel polish. |

#### Person 2 — Seating/Inventory (core delivery) ⚠️ CRITICAL PATH

| Day | Deliverable |
|---|---|
| 18–20 | **US-016: Reservation creation with pessimistic lock.** POST `/v1/reservations` atomically sets Seat to RESERVED, creates Reservation (10 min TTL), increments Batch.sold. Concurrent request returns 409. ReservationCreated published. **This is the #1 critical path item — Orders and Payments cannot integrate without it.** |
| 21–23 | US-017: Expiration job. Reservation → EXPIRED, Seat → AVAILABLE, Batch.sold decremented. ReservationExpired published to RabbitMQ. Order consumer transitions Order to EXPIRED. |
| 24–25 | US-021: Platform Operator override (DELETE reservation). AuditLog written. |
| 26–27 | Load test: simulate 100 concurrent Reservation requests on same Seat. Verify zero overbooking. Fix any race conditions. |

#### Person 3 — Orders + Payments (core delivery) ⚠️ CRITICAL PATH

| Day | Deliverable |
|---|---|
| 18–20 | **US-018: Order creation.** POST `/v1/orders` creates Order with CREATED status, linked to real Reservation (Person 2's endpoint). Price resolution with Batch price + Service Fee. OrderCreated published. Idempotency key validated. |
| 21–23 | **US-019: Payment processing.** POST `/v1/payments` creates Payment with INITIATED, calls gateway. On success: AUTHORIZED, PaymentAuthorized published, Order → CONFIRMED. Webhook endpoint live. |
| 24–25 | US-020: Payment failure handling. PaymentFailed published. Reservation released via event. Buyer notified. |
| 26–27 | US-022: Checkout Flow UI (Angular). Seat selection → Order summary → Payment form → Confirmation. 10-minute countdown. Error handling. |

#### Person 4 — Ticket Issuance + Notifications (integration)

| Day | Deliverable |
|---|---|
| 18–21 | US-023: Ticket issuance consumer. Listens for PaymentAuthorized (real event from Person 3). Creates Ticket with ISSUED status, dynamic QR, accessPolicy. TicketIssued published. Idempotency key prevents double issuance. |
| 22–24 | US-026: Notification dispatch. Consumes TicketIssued, ReservationExpired, PaymentFailed. Creates and dispatches Email Notifications. Dead-letter queue handles failures. |
| 25–27 | US-024: Ticket cancelation endpoint. US-025: Support resend endpoint. US-036 prep: TicketInvalidated consumer scaffolded. |

---

## Sprint 3 — Days 28–37: Ticket Lifecycle + Buyer Portal

### Per-person deliverables

#### Person 1 — Panels polish + Catalog

| Day | Deliverable |
|---|---|
| 28–30 | Organizer Panel: Seating Map config UI (Person 2's API), Batch management, order list view, Payout status view. |
| 31–33 | Super Admin Panel: Event approval workflow polish, user management polish, platform-wide views. |
| 34–37 | Public Event Catalog frontend (Angular): event listing with filters, event detail page, seat map preview. |

#### Person 2 — Seating Map UI + Support Person 3

| Day | Deliverable |
|---|---|
| 28–31 | Interactive seat map component (Angular) for Buyer seat selection. Integrate with GET seats endpoint and POST reservation endpoint. |
| 32–34 | Batch management UI in Organizer Panel. Batch status display (SCHEDULED/ACTIVE/EXHAUSTED/EXPIRED). |
| 35–37 | Support integration testing. Help Person 1 integrate seat map into checkout flow. Fix Reservation edge cases. |

#### Person 3 — Payout + Checkout Polish

| Day | Deliverable |
|---|---|
| 28–30 | US-028: Payout generation. Scheduled job detects Date passed. Aggregates payments. Creates Payout → PayoutGenerated published. Settlement to Organizer → PayoutProcessed published. Retry logic on failure. |
| 31–33 | Checkout flow full integration: seat selection (Person 2 API) → Order creation → Payment → Confirmation screen. |
| 34–37 | Payment edge cases: concurrent payment race, expired Order during payment, gateway timeout handling. End-to-end test of full purchase flow. |

#### Person 4 — Buyer Portal + Notification Polish

| Day | Deliverable |
|---|---|
| 28–30 | US-027: Buyer Portal (Angular). Ticket list with dynamic QR display. Order history. Ticket download as PDF. |
| 31–33 | Notification templates for: Ticket delivered, Reservation expired, Payment failed, Event approved/rejected. Template rendering verified per channel. |
| 34–37 | US-029 prep: Access Control Validation endpoint integrated with real Ticket Issuance lookup. Scan → Ticket lookup ≤ 100 ms verified. |

---

## Sprint 4 — Days 38–45: Operations + Stabilization

### Per-person deliverables

#### Person 1 — Gateway hardening + Demo prep

| Day | Deliverable |
|---|---|
| 38–39 | Rate limiting configuration on Spring Cloud Gateway. Anti-bot CAPTCHA on registration and checkout. |
| 40–41 | Cross-service integration testing. Verify all gateway routes. Fix auth/CORS issues. |
| 42–45 | Demo environment preparation. Seed data: one Organizer, one Event, one Date, Seating Map, Batch. Smoke test full flow. |

#### Person 2 — Stabilization + Load testing

| Day | Deliverable |
|---|---|
| 38–39 | Load test: 100+ concurrent reservations on same Date. Zero overbooking verified. Performance metrics captured. |
| 40–41 | Fix any race conditions or performance issues discovered. Batch expiration edge cases. |
| 42–45 | US-037 (integration): participate in end-to-end integration. Fix Seating/Inventory issues blocking the demo flow. |

#### Person 3 — Stabilization + Financial reports data

| Day | Deliverable |
|---|---|
| 38–39 | Payout edge cases. Payment reconciliation verification. Gateway error handling hardening. |
| 40–41 | Organizer Panel: payout status view, order list view. Export functionality for Finance. |
| 42–45 | US-037 (integration): participate in end-to-end integration. Fix Payment issues blocking the demo flow. |

#### Person 4 — Access Control + Reporting + Validator App ⚠️ CRITICAL PATH

| Day | Deliverable |
|---|---|
| 38–39 | **US-029 + US-030: QR Validation live.** POST `/v1/validations` with real Ticket lookup. Success and failure paths. ≤ 100 ms target verified. |
| 40 | US-031: Offline sync endpoint. POST `/v1/validations/sync`. First-scan-wins. ConflictDetected published. |
| 41 | US-036: Ticket invalidation consumer. Consumes ConflictDetected → Ticket status to INVALIDATED. |
| 42 | US-032: Validator App (Angular). QR scanner (camera API). GRANTED/DENIED visual. Offline queue indicator. |
| 43 | US-033 + US-034 + US-035: Reporting endpoints. Sales, Commission, Access reports. Event projection consumers verified. |
| 44–45 | US-037: Full end-to-end demo flow. Fix remaining issues. Final stabilization. |

---

## Critical Path

The following chain is the longest dependency sequence. Any delay in this chain delays the final demo.

```
Day 3:  Database migrations and gateway routes (all)
   ↓
Day 5:  Mocks available for all cross-service calls (all)
   ↓
Day 10: JWT auth live in gateway (Person 1) — BLOCKER for everyone
   ↓
Day 15: EventReleased Flow complete (Person 1) — BLOCKER for seat availability
   ↓
Day 20: Reservation endpoint live with locking (Person 2) — BLOCKER for Orders
   ↓
Day 23: PaymentAuthorized event live (Person 3) — BLOCKER for Ticket Issuance
   ↓
Day 21: Ticket issuance consumer live (Person 4) — BLOCKER for Access Control
   ↓
Day 39: QR Validation endpoint live (Person 4) — REQUIRED for demo
   ↓
Day 45: Full demo flow verified (all)
```

**Flags:**
- ⚠️ If Person 1 does not deliver JWT auth by Day 10, all other work is blocked.
- ⚠️ If Person 2 does not deliver Reservation by Day 20, Orders cannot integrate.
- ⚠️ If Person 3 does not deliver PaymentAuthorized by Day 23, Ticket Issuance is blocked.
- ⚠️ If Person 4 does not deliver QR Validation by Day 39, the demo cannot show the entry flow.

---

## MVP Cut Line — Day 45 Demo Scenario

**The following must work for a successful demo:**

1. **Super Admin** logs in → creates a test Organizer account.
2. **Organizer** logs in → creates an Event with one Date → configures Seating Map → creates a Batch → submits Event for review.
3. **Platform Operator** (or Super Admin) approves the Event → Event is RELEASED.
4. **Buyer** registers → browses Event catalog → selects a Seat → Reservation created (10-min hold) → proceeds to checkout → Order created → submits Payment → Payment authorized.
5. **System** issues Ticket with dynamic QR → Notification delivered (email).
6. **Buyer** views Ticket in Buyer Portal with QR code.
7. **Door Validator** opens Validator App → scans QR → entry GRANTED → Ticket status = USED.

**If any step in this chain fails, the demo fails.**

### What can be cut if behind schedule (in order of sacrifice):

1. SMS/WhatsApp Notifications → Email only is sufficient.
2. Offline sync (US-031) → Real-time validation only.
3. Reporting endpoints (US-033, US-034, US-035) → hardcoded demo data.
4. Organizer Panel polish → raw API calls via Swagger UI.
5. Interactive seat map UI → simple seat list with click-to-select.

### What CANNOT be cut:

- Reservation with concurrency protection.
- Payment processing (can use a mock gateway if real integration not ready).
- Ticket issuance with QR.
- QR validation at the door.

---

## Risk Table

| # | Risk | Severity | Mitigation |
|---|---|---|---|
| 1 | **Seat contention under concurrency** — Race conditions cause overbooking. | Critical | Pessimistic lock on Seat row. Load test with 100+ concurrent requests before Sprint 2 ends. Zero-tolerance — no overbooking (NFR-017). |
| 2 | **Payment gateway integration delays** — Guatemala-compatible gateway SDK takes longer than expected. | High | Have a mock gateway ready by Day 15. Gateway integration can be swapped to mock for demo without blocking Ticket Issuance flow. |
| 3 | **Person 1 delayed on Identity/Auth** — JWT auth is the #1 cross-service dependency. Late delivery blocks everyone. | High | JWT mock available from Day 5 (setup week). If auth endpoint delayed beyond Day 12, all other services continue with mock JWT. Person 1 escalates immediately per 2-hour rule. |
| 4 | **4 people for 9 services in 45 days** — Under-delivery risk is real. | High | Strict MVP cut line defined above. Sacrifice list ordered. Core transaction flow (seats → orders → payment → ticket → scan) is non-negotiable; everything else is sacrificable. |
| 5 | **Offline sync conflict resolution complexity** — First-scan-wins logic with offline queue is architecturally complex. | Medium | US-031 (offline sync) is the first item to cut if behind schedule. Real-time validation (US-029, US-030) is sufficient for the demo. Offline sync can be delivered post-demo. |
