You have full context from Phase 0, 1, and 2 already in this session. Using that context exclusively, produce the following Phase 3 architecture files. Do not re-read previous files unless you need to verify a specific detail. Do not infer or invent — if a decision is not documented, mark it STATUS: PENDING — requires team decision and describe what needs to be resolved.
docs/phase-3/non-functional-requirements.md
Formalize DISCOVERY.md Block 8 into measurable specs. Format: ID (NFR-XXX), category, description, acceptance criteria, bounded contexts affected. Categories: Performance, Availability, Scalability, Security, Observability, Consistency.
docs/phase-3/adrs/
One ADR per decision already made in Phase 0 and Phase 1. Format: title, date, status, context, options considered, decision, justification, consequences. Document these decisions:

Microservices as architectural style
No refunds in v1
No ticket transfer or resale in v1
Reservation is universal (mapped and general admission)
Atomic Batch and Reservation consistency
Dynamic QR with 2-minute TTL
First-scan-wins for offline sync conflicts
Idempotency key composition strategy
Payout triggered automatically after Date passes
Catalog collapsed into Event Management
Pricing collapsed into Orders
AuditLog as cross-cutting concern
One-way relationship between ValidationRecord and Ticket

For undecided infrastructure ADRs (message broker, API gateway, cloud provider, container orchestration) create the file with STATUS: PENDING, document the options to evaluate, and the criteria for deciding.
docs/phase-3/event-schemas.md
Formal payload schema for every domain event from the events map. Per event: name, producer, consumers, trigger, full JSON schema. Use aggregate field definitions as source of truth for field names and types.
docs/phase-3/service-contracts.md
REST API contract for every synchronous interaction from bounded contexts and critical flows. Per endpoint: owner service, method, path, parameters, request body, response schema, error codes, related use case. All paths prefixed /v1/.
docs/phase-3/data-ownership-map.md
One entry per aggregate: owning service, owned entities, external references (IDs only), which services may read it and how (API or event projection).
docs/phase-3/er-diagrams/
One Mermaid erDiagram per bounded context. Entities owned by that service only. Cross-service references as ID fields only — no foreign keys across boundaries. Files: identity.md, event-management.md, seating-inventory.md, orders.md, payments.md, ticket-issuance.md, access-control.md, notifications.md, reporting.md.
docs/phase-3/deployment-diagram.md
Mermaid diagram showing all 9 services, cross-cutting infrastructure (AuditLog, Notifications, Observability), API gateway, message broker, and client-facing frontends (buyer portal, organizer panel, super admin panel, validator app). Mark undecided technology as STATUS: PENDING.