# Non-Functional Requirements

> **Phase:** 3 — Architecture  
> **Source:** `docs/phases/phase-0/discovery.md` Block 7, Block 8; `docs/phases/phase-2/business-rules.md`  
> **Format:** ID, Category, Description, Acceptance Criteria, Bounded Contexts Affected

---

## Performance

| ID | Description | Acceptance Criteria | Bounded Contexts Affected |
|---|---|---|---|
| NFR-001 | Purchase / checkout end-to-end latency | p99 ≤ 1,000 ms from seat selection to payment confirmation under normal load (5,000 concurrent users). | Seating/Inventory, Orders, Payments |
| NFR-002 | Ticket issuance latency | p99 ≤ 500 ms from PaymentAuthorized to TicketIssued event fired. | Ticket Issuance |
| NFR-003 | QR Validation at door latency | p99 ≤ 100 ms from scan to response (granted/denied) under real-time mode. | Access Control |
| NFR-004 | Seat hold acquisition latency | p99 ≤ 200 ms for atomic Reservation creation including Batch.sold increment under peak contention. | Seating/Inventory |
| NFR-005 | Event catalog page load | p99 ≤ 500 ms for the public-facing Event listing read model. | Event Management (Catalog read model) |

---

## Availability

| ID | Description | Acceptance Criteria | Bounded Contexts Affected |
|---|---|---|---|
| NFR-006 | Platform uptime SLA | 99.9% availability measured monthly (~8.7 hours maximum downtime/year, ~43.8 minutes/month). | All |
| NFR-007 | Recovery Time Objective (RTO) | ≤ 1 hour from failure detection to full service restoration. | All |
| NFR-008 | Recovery Point Objective (RPO) | RPO ≤ 24 hours. Daily backups are sufficient for v1. | All |
| NFR-009 | Independent service failures | A failure in one service must not cascade to unrelated services. Circuit breakers or bulkhead patterns required on all synchronous inter-service calls. | All |

---

## Scalability

| ID | Description | Acceptance Criteria | Bounded Contexts Affected |
|---|---|---|---|
| NFR-010 | Normal load capacity | System sustains 5,000 concurrent users and 100 transactions/minute without degradation. | All |
| NFR-011 | Peak load capacity | System sustains 30,000 concurrent users and 1,000 transactions/minute without degradation (major presale / sports final scenario). | Seating/Inventory, Orders, Payments, Ticket Issuance |
| NFR-012 | Anti-bot and rate limiting | Virtual queue activates for high-demand Events. Rate limiting enforced per User and per IP. Anti-bot protection (CAPTCHA or equivalent) on purchase flow entry. | Seating/Inventory, Orders, Identity |
| NFR-013 | Horizontal scalability of contention services | Seating/Inventory and Orders must scale horizontally. Seat-level locking strategy (pessimistic or distributed lock) must prevent contention bottlenecks. | Seating/Inventory, Orders |

---

## Security

| ID | Description | Acceptance Criteria | Bounded Contexts Affected |
|---|---|---|---|
| NFR-014 | No raw card data storage | Platform must not store raw card numbers, CVVs, or full magnetic stripe data. Payment gateway handles PCI-scoped data. System stores only gatewayReference tokens. | Payments |
| NFR-015 | PII protection | Personal user data (email, phone, full name) encrypted at rest and in transit. Access restricted to authorized Roles only. | Identity, Orders, Ticket Issuance |
| NFR-016 | Full audit trail | Every sensitive change records: actorId, actorRole, action, targetEntity, targetId, previousState (JSON), newState (JSON), occurredAt. AuditLog is append-only and immutable. | All (cross-cutting) |
| NFR-017 | Zero-tolerance: no seat overbooking | The system must guarantee that no Seat is sold more than once. Verified by invariant checks and load tests before launch. | Seating/Inventory |
| NFR-018 | Zero-tolerance: no double Ticket issuance | No two Tickets may be issued for the same Seat in the same Date. Enforced by idempotency key at Ticket Issuance boundary. | Ticket Issuance |
| NFR-019 | Zero-tolerance: no duplicate QR Validation | The same Ticket must not be accepted twice at the door. First-scan-wins rule enforced in both real-time and offline sync modes. | Access Control |
| NFR-020 | Zero-tolerance: no system crash at presale spike | System must not crash or become unresponsive during event launch / presale. Virtual queue and back-pressure mechanisms required. | Seating/Inventory, Orders |
| NFR-021 | Zero-tolerance: no lost or unrecoverable Orders | Every Order must be recoverable. No write is silently dropped. Idempotency and event sourcing or WAL required on critical paths. | Orders, Payments |
| NFR-022 | Data protection compliance | System complies with Guatemalan data protection law. Sales terms and return policies (no refunds) must be explicitly accepted at checkout. Marketing consent must be opt-in. | Identity, Orders |

---

## Observability

| ID | Description | Acceptance Criteria | Bounded Contexts Affected |
|---|---|---|---|
| NFR-023 | Structured logging from day 1 | All services emit structured JSON logs with correlation IDs from the first deployed service. | All |
| NFR-024 | Distributed tracing | OpenTelemetry traces propagated across all synchronous and asynchronous inter-service calls. End-to-end trace from Buyer action to final outcome. | All |
| NFR-025 | Business metrics per service | Each service exposes domain-specific metrics (e.g., Reservations created/expired per minute, Payments authorized/failed per minute, Validations per second). | All |
| NFR-026 | Alerting on SLA breaches | Automated alerts when latency targets (NFR-001 through NFR-005) are exceeded, when error rates cross threshold, or when availability drops below 99.9%. | All |
| NFR-027 | Central operational dashboard | Single Grafana Cloud dashboard aggregating health, latency, throughput, and error rate across all services. Accessible to Platform Operator, Super Admin, and DevOps team. | All (infrastructure) |

---

## Consistency

| ID | Description | Acceptance Criteria | Bounded Contexts Affected |
|---|---|---|---|
| NFR-028 | Ticket inventory: strong / real-time consistency | Seat status and Reservation state are always consistent. No stale reads allowed on Seat availability during active sales. | Seating/Inventory |
| NFR-029 | Order status: strong / real-time consistency | Order lifecycle transitions (CREATED → CONFIRMED → EXPIRED) are immediately visible to all consumers. | Orders |
| NFR-030 | Payment confirmation: strong / real-time consistency | PaymentAuthorized and PaymentFailed events reflect the true state of the gateway transaction with no delay. | Payments |
| NFR-031 | Access Validations: strong / real-time consistency | ValidationRecord results are immediately visible to prevent duplicate entry. First-scan-wins enforced atomically. | Access Control |
| NFR-032 | Reports and metrics: eventual consistency | Report projections (SalesReport, CommissionReport) may lag behind source events. Acceptable lag ≤ 30 seconds under normal load. | Reporting |
| NFR-033 | Notification logs: eventual consistency | Notification delivery status may lag. No business logic depends on Notification delivery. | Notifications |
| NFR-034 | Audit logs: eventual consistency (append-only) | AuditLog entries may arrive with slight delay but must never be lost, modified, or deleted. | All (cross-cutting) |
