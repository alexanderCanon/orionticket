You have full context from Phases 0 through 3 in this session. Produce the following five files. Use only what is explicitly documented. Do not infer or expand beyond what is defined.

docs/phase-4/definition-of-done.md
Define what "done" means for this team. Include criteria for: a user story, a backend service endpoint, a frontend component, a domain event, an ER diagram implementation, and a full service deployment. Base criteria on the stack: Java Spring Boot, Angular, Docker Compose, RabbitMQ, Spring Cloud Gateway, VPS. Criteria must be concrete and checkable — not abstract principles.

docs/phase-4/product-backlog.md
Produce a prioritized product backlog derived exclusively from docs/phase-2/functional-requirements.md and the MVP scope defined in DISCOVERY.md Block 12.
Format each item as:

ID: US-XXX
As a [actor] I want to [action] so that [value]
Bounded context: which service owns it
Priority: Critical / High / Medium / Low
Dependencies: US-XXX, US-XXX
Estimated complexity: S / M / L / XL

Group by sprint using this four-sprint structure:

Sprint 1 — Foundation: Identity, Event Management, basic organizer panel
Sprint 2 — Core transaction: Seating/Inventory, Orders, Payments
Sprint 3 — Ticket lifecycle: Ticket Issuance, Notifications, buyer portal
Sprint 4 — Operations: Access Control, Reporting, stabilization

Mark items explicitly excluded from MVP as BACKLOG — v2 with a brief reason.

project/TEAM.md
Produce the team coordination guide. Include:

Service ownership — assign each bounded context to one of four team members (Person 1 through 4). Each person owns their services end-to-end: backend, tests, and their frontend slice. Base the split on the layer dependency order: Identity/Event Management → Seating/Inventory → Orders/Payments → Ticket Issuance/Access Control/Notifications/Reporting.
Branching strategy — define branch naming, PR rules, and merge policy for a 4-person team on GitHub.
Daily workflow — standup format (15 min max), three questions, async fallback for university schedules.
Contract-first rule — no service implementation starts until its API contract and event schemas are published in docs/phase-3/. Every cross-service dependency must have a mock before Sprint 1 ends.
Escalation rule — if blocked for more than 2 hours, escalate immediately. Do not wait for standup.
Definition of Done reference — link to docs/phase-4/definition-of-done.md.
Documentation index reference — link to INDEX.md.


project/implementation-guide.md
Produce a 45-day implementation strategy based on the four-sprint structure and team ownership defined above. Include:

Timeline — day-by-day breakdown for Days 1–7 (setup week, all four people together), then sprint-by-sprint from Day 8.
Setup week goals — what must exist before anyone writes production code: local environment running, all service contracts published, all mocks scaffolded, RabbitMQ running locally, Spring Cloud Gateway configured, GitHub Projects board populated.
Per-sprint goals — what each person delivers by end of sprint. Be specific: not "implement Orders" but "OrderCreated event published to RabbitMQ, PaymentInitiated endpoint live, idempotency key validated."
Critical path — identify the sequence of tasks where any delay cascades. Flag them explicitly.
MVP cut line — what must work for a successful demo at Day 45. Define the demo scenario: one organizer, one event, one buyer purchases a ticket, Door Validator scans QR successfully.
Risk table — top 5 risks with mitigation strategy, taken from DISCOVERY.md and Phase 3 ADRs.


INDEX.md (repo root)
Produce a documentation index structured as a book. Each phase is a chapter. Each file is a section with: path, one-sentence description of what it contains, and when to read it.
At the top include a quick-reference section with these five questions answered with a direct file link and section:

"What does [term] mean in this system?" → ubiquitous-language.md
"Who owns [entity] data?" → data-ownership-map.md
"What events does [service] produce?" → domain-events-map.md
"What are my Definition of Done criteria?" → definition-of-done.md
"What do I implement this sprint?" → implementation-guide.md

At the bottom include a clear section separator between:

System Specification — docs/ — timeless, version-controlled, does not expire
Project Management — project/ — time-bound, updated as the project evolves

This file replaces tribal knowledge. If it is not in the index, it does not exist.