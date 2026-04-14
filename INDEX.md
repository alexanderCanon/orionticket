# OrionTicket — Documentation Index

> **This file replaces tribal knowledge. If it is not in the index, it does not exist.**

---

## Quick Reference

| Question | Answer |
|---|---|
| **"What does [term] mean in this system?"** | [docs/phases/phase-1/ubiquitous-language-glossary.md](docs/phases/phase-1/ubiquitous-language-glossary.md) — 21 terms, use only these in code, docs, and conversation. |
| **"Who owns [entity] data?"** | [docs/phases/phase-3/data-ownership-map.md](docs/phases/phase-3/data-ownership-map.md) — one entry per aggregate: owning service, owned entities, external references, read mechanism. |
| **"What events does [service] produce?"** | [docs/phases/phase-1/domain-events-map.md](docs/phases/phase-1/domain-events-map.md) — every domain event grouped by context. Full JSON schemas in [docs/phases/phase-3/event-schemas.md](docs/phases/phase-3/event-schemas.md). |
| **"What are my Definition of Done criteria?"** | [docs/phases/phase-4/definition-of-done.md](docs/phases/phase-4/definition-of-done.md) — checkable criteria for user stories, endpoints, components, events, migrations, and deployments. |
| **"What do I implement this sprint?"** | [project/implementation-guide.md](project/implementation-guide.md) — day-by-day breakdown per person per sprint. |

---

## System Specification

> `docs/` — Timeless, version-controlled, does not expire.  
> These documents define **what** the system is and **why** decisions were made.

---

### Chapter 0 — Discovery

The business definition of OrionTicket. Read this first. Everything downstream derives from this document.

| Section | Path | Description | When to read |
|---|---|---|---|
| Discovery Document | [docs/phases/phase-0/discovery.md](docs/phases/phase-0/discovery.md) | Complete business requirements: vision, market scope, event/ticket domain, purchase flow, roles, payments, security, scalability, integrations, team constraints, and phased roadmap. | Before any other document. When you need to understand a business decision or constraint. |

---

### Chapter 1 — Domain Modeling

The domain language, events, bounded contexts, and aggregate structure. Read after Discovery.

| Section | Path | Description | When to read |
|---|---|---|---|
| Ubiquitous Language Glossary | [docs/phases/phase-1/ubiquitous-language-glossary.md](docs/phases/phase-1/ubiquitous-language-glossary.md) | 21 canonical terms used across code, docs, and conversation. No synonyms allowed. | When naming anything. When reviewing code or writing docs. |
| Domain Events Map | [docs/phases/phase-1/domain-events-map.md](docs/phases/phase-1/domain-events-map.md) | Every domain event in the system, grouped by context. | When implementing an event publisher or consumer. |
| Bounded Context Diagrams | [docs/phases/phase-1/bounded-context-diagrams.md](docs/phases/phase-1/bounded-context-diagrams.md) | The 9 bounded contexts, what each owns, and what changed from Discovery (Catalog dropped, Pricing absorbed, Batches moved). | When understanding service boundaries and why they exist. |
| Aggregate Definitions | [docs/phases/phase-1/aggregate-definitions.md](docs/phases/phase-1/aggregate-definitions.md) | Every aggregate root with its fields, child entities, types, and statuses. The source of truth for data modeling. | When implementing an entity, writing a migration, or designing an API response. |

---

### Chapter 2 — Use Cases & Flows

Who uses the system, what they do, and how the system behaves. Read after Chapter 1.

| Section | Path | Description | When to read |
|---|---|---|---|
| Actor–Role Map | [docs/phases/phase-2/actor-role-map.md](docs/phases/phase-2/actor-role-map.md) | All 9 actors mapped to bounded contexts, aggregates, and read/write operations with permission scopes. | When implementing authorization. When adding a new endpoint. |
| Use Case Catalog | [docs/phases/phase-2/use-case-catalog.md](docs/phases/phase-2/use-case-catalog.md) | 19 use cases with actor, preconditions, main flow, alternative flows, postconditions, and domain events fired. | When writing acceptance criteria. When understanding a user story's expected behavior. |
| Critical Flows | [docs/phases/phase-2/critical-flows.md](docs/phases/phase-2/critical-flows.md) | 5 Mermaid sequence diagrams: event creation, purchase flow, reservation expiry, QR validation (success + failure), offline sync. | When implementing a cross-service flow. When debugging an integration issue. |
| Business Rules | [docs/phases/phase-2/business-rules.md](docs/phases/phase-2/business-rules.md) | 47 numbered rules grouped by bounded context, each citing its source artifact. | When implementing validation logic. When questioning why a constraint exists. |
| Functional Requirements | [docs/phases/phase-2/functional-requirements.md](docs/phases/phase-2/functional-requirements.md) | UML use case diagram (Mermaid) showing all actors and their use case associations per bounded context. | When reviewing system scope at a glance. |

---

### Chapter 3 — Architecture

Technical decisions, API contracts, event schemas, data models, and deployment topology. Read when building.

| Section | Path | Description | When to read |
|---|---|---|---|
| Non-Functional Requirements | [docs/phases/phase-3/non-functional-requirements.md](docs/phases/phase-3/non-functional-requirements.md) | 34 NFRs across Performance, Availability, Scalability, Security, Observability, and Consistency with measurable acceptance criteria. | When making a performance or infrastructure decision. When writing load tests. |
| Design Principles and Patterns | [docs/phases/phase-3/design-principles-and-patters.md](docs/phases/phase-3/design-principles-and-patters.md) | Engineering principles and design patterns applied across the system. | When making a design decision in code. |
| Service Contracts | [docs/phases/phase-3/service-contracts.md](docs/phases/phase-3/service-contracts.md) | REST API contract for every synchronous endpoint: method, path, request/response schemas, error codes, related use case. | When implementing or consuming any API endpoint. The definitive reference. |
| Event Schemas | [docs/phases/phase-3/event-schemas.md](docs/phases/phase-3/event-schemas.md) | Full JSON payload schema for all 33 domain events: producer, consumers, trigger, field definitions. | When publishing or consuming a domain event from RabbitMQ. |
| Data Ownership Map | [docs/phases/phase-3/data-ownership-map.md](docs/phases/phase-3/data-ownership-map.md) | One entry per aggregate: owning service, owned entities, external ID references, consumer services, read mechanism (API vs event projection). | When querying data you don't own. When deciding sync vs async for a cross-service read. |
| Deployment Diagram | [docs/phases/phase-3/deployment-diagram.md](docs/phases/phase-3/deployment-diagram.md) | Mermaid diagram of all 9 services, frontends, databases, RabbitMQ, Spring Cloud Gateway, external integrations, and cross-cutting infrastructure. | When setting up infrastructure or debugging connectivity. |

#### Architecture Decision Records (ADRs)

| ADR | Path | Decision |
|---|---|---|
| ADR-001 | [adrs/ADR-001-microservices-architecture.md](docs/phases/phase-3/adrs/ADR-001-microservices-architecture.md) | Microservices from day 1 (client constraint). |
| ADR-002 | [adrs/ADR-002-no-refunds-v1.md](docs/phases/phase-3/adrs/ADR-002-no-refunds-v1.md) | No refunds in v1. All sales final. |
| ADR-003 | [adrs/ADR-003-no-transfer-resale-v1.md](docs/phases/phase-3/adrs/ADR-003-no-transfer-resale-v1.md) | No ticket transfer or resale in v1. |
| ADR-004 | [adrs/ADR-004-universal-reservation.md](docs/phases/phase-3/adrs/ADR-004-universal-reservation.md) | Unified Seat aggregate with type discriminator (MAPPED / GENERAL_ADMISSION). |
| ADR-005 | [adrs/ADR-005-atomic-batch-reservation.md](docs/phases/phase-3/adrs/ADR-005-atomic-batch-reservation.md) | Batch.sold incremented atomically with Reservation creation. |
| ADR-006 | [adrs/ADR-006-dynamic-qr-ttl.md](docs/phases/phase-3/adrs/ADR-006-dynamic-qr-ttl.md) | Dynamic QR code with 2-minute TTL. |
| ADR-007 | [adrs/ADR-007-first-scan-wins.md](docs/phases/phase-3/adrs/ADR-007-first-scan-wins.md) | First-scan-wins for offline sync conflicts. |
| ADR-008 | [adrs/ADR-008-idempotency-key.md](docs/phases/phase-3/adrs/ADR-008-idempotency-key.md) | Composite idempotency key: txnId + buyerId + eventId + seatId + timestamp. |
| ADR-009 | [adrs/ADR-009-payout-after-date.md](docs/phases/phase-3/adrs/ADR-009-payout-after-date.md) | Payout triggered automatically after Event Date passes. |
| ADR-010 | [adrs/ADR-010-catalog-collapsed.md](docs/phases/phase-3/adrs/ADR-010-catalog-collapsed.md) | Catalog collapsed into Event Management as a read model. |
| ADR-011 | [adrs/ADR-011-pricing-collapsed.md](docs/phases/phase-3/adrs/ADR-011-pricing-collapsed.md) | Pricing collapsed into Orders. |
| ADR-012 | [adrs/ADR-012-auditlog-cross-cutting.md](docs/phases/phase-3/adrs/ADR-012-auditlog-cross-cutting.md) | AuditLog is cross-cutting, written by every service, owned by none. |
| ADR-013 | [adrs/ADR-013-validation-ticket-one-way.md](docs/phases/phase-3/adrs/ADR-013-validation-ticket-one-way.md) | One-way reference from ValidationRecord to Ticket. |
| ADR-014 | [adrs/ADR-014-message-broker.md](docs/phases/phase-3/adrs/ADR-014-message-broker.md) | RabbitMQ. |
| ADR-015 | [adrs/ADR-015-api-gateway.md](docs/phases/phase-3/adrs/ADR-015-api-gateway.md) | Spring Cloud Gateway. |
| ADR-016 | [adrs/ADR-016-cloud-provider.md](docs/phases/phase-3/adrs/ADR-016-cloud-provider.md) | VPS (self-hosted). |
| ADR-017 | [adrs/ADR-017-container-orchestration.md](docs/phases/phase-3/adrs/ADR-017-container-orchestration.md) | Docker Compose. |

#### ER Diagrams (one per bounded context)

| Service | Path |
|---|---|
| Identity | [er-diagrams/identity.md](docs/phases/phase-3/er-diagrams/identity.md) |
| Event Management | [er-diagrams/event-management.md](docs/phases/phase-3/er-diagrams/event-management.md) |
| Seating / Inventory | [er-diagrams/seating-inventory.md](docs/phases/phase-3/er-diagrams/seating-inventory.md) |
| Orders | [er-diagrams/orders.md](docs/phases/phase-3/er-diagrams/orders.md) |
| Payments | [er-diagrams/payments.md](docs/phases/phase-3/er-diagrams/payments.md) |
| Ticket Issuance | [er-diagrams/ticket-issuance.md](docs/phases/phase-3/er-diagrams/ticket-issuance.md) |
| Access Control | [er-diagrams/access-control.md](docs/phases/phase-3/er-diagrams/access-control.md) |
| Notifications | [er-diagrams/notifications.md](docs/phases/phase-3/er-diagrams/notifications.md) |
| Reporting | [er-diagrams/reporting.md](docs/phases/phase-3/er-diagrams/reporting.md) |

---

### Chapter 4 — Backlog & Planning

What to build, in what order, and what "done" means.

| Section | Path | Description | When to read |
|---|---|---|---|
| Definition of Done | [docs/phases/phase-4/definition-of-done.md](docs/phases/phase-4/definition-of-done.md) | Checkable criteria for: user stories, backend endpoints, frontend components, domain events, ER implementations, and service deployments. | Before marking any item as complete. During PR review. |
| Product Backlog | [docs/phases/phase-4/product-backlog.md](docs/phases/phase-4/product-backlog.md) | 37 user stories across 4 sprints + 10 v2 backlog items. Prioritized by dependency order and business value. | During sprint planning. When picking up a new story. |

---

---

## Project Management

> `project/` — Time-bound, updated as the project evolves.  
> These documents define **how** the team works and **when** things get delivered.

| Section | Path | Description | When to read |
|---|---|---|---|
| Team Coordination Guide | [project/TEAM.md](project/TEAM.md) | Service ownership per person, branching strategy, PR rules, daily standup format, contract-first rule, escalation protocol. | Day 1 of every sprint. When onboarding. When a process question arises. |
| Implementation Guide | [project/implementation-guide.md](project/implementation-guide.md) | 45-day strategy: day-by-day setup week, per-sprint per-person deliverables, critical path, MVP cut line, demo scenario, risk table. | Every morning. When deciding what to work on today. When assessing progress. |
