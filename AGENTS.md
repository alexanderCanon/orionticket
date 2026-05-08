# OrionTicket Agent Guide

## Operating Principles

- Before making changes, inspect the repository for implementation projects, package manifests, build wrappers, runtime files, and documentation that define the relevant scope.
- Treat `docs/` as the source of truth. Do not invent architecture, commands, services, fields, events, or conventions that contradict the documents.
- If a request is ambiguous or vague, stop and ask clarifying questions before making changes.
- If code is requested, write code in English and document it well.
- This guide defines the shared operating standard for the team before implementation work begins.

## Product and Architecture

- Product: OrionTicket, a white-label ticket sales platform for massive events.
- Architectural decision: microservices from day 1. This is a client constraint documented in `docs/phases/phase-3/adrs/ADR-001-microservices-architecture.md`.
- Target stack documented in the project:
  - Java 21
  - Spring Boot 3
  - Angular 17+
  - Docker Compose
  - PostgreSQL, one database per service
  - Flyway for schema migrations
  - RabbitMQ for asynchronous domain events
  - Spring Cloud Gateway for external routing
  - Spring Security, Spring Boot Actuator, OpenAPI/Swagger
  - Testcontainers for integration tests
  - OpenTelemetry, structured JSON logs, metrics/dashboarding
  - VPS deployment target for the MVP, unless superseded by a documented architecture decision

## Required Reading Order

Before implementing or changing anything beyond small documentation edits, read:

1. `docs/INDEX.md`
2. `docs/project/services/TECHNICAL_INDEX.md`
3. `docs/project/services/shared-foundation.md`
4. `docs/standards/spring-shared-foundation-standard.md`
5. `docs/standards/spring-microservices-production-guide.md`
6. The relevant service manual under `docs/project/services/`
7. The relevant API, event, ER, and ADR documents under `docs/phases/`

For planning and team process, also read:

- `docs/project/TEAM.md`
- `docs/project/implementation-guide.md`
- `docs/phases/phase-4/definition-of-done.md`
- `docs/phases/phase-4/product-backlog.md`

## Repository Structure

- `docs/README.md`: short project philosophy statement.
- `docs/INDEX.md`: central documentation index. If something is not indexed or documented, treat it as undefined.
- `docs/phases/phase-0/`: discovery and business context.
- `docs/phases/phase-1/`: domain model, ubiquitous language, aggregates, bounded contexts, domain events.
- `docs/phases/phase-2/`: actors, use cases, critical flows, business rules, functional requirements.
- `docs/phases/phase-3/`: architecture, API contracts, event schemas, data ownership, deployment diagram, ER diagrams, ADRs.
- `docs/phases/phase-4/`: Definition of Done and product backlog.
- `docs/project/`: operational project material, team ownership, implementation plan, ceremonies.
- `docs/project/services/`: service-by-service technical manuals and shared foundation.
- `docs/standards/`: Spring and architecture implementation standards.
- `docs/archive/`: historical prompts and old material. Use for context only; current docs under `docs/phases/`, `docs/project/`, and `docs/standards/` take priority.
- `.agents/skills/`: local agent skills. Use them when their trigger conditions match the task.

## Services and Ownership Boundaries

The documented service boundaries are:

- Identity
- Event Management, including the Catalog read model
- Seating / Inventory
- Orders
- Payments
- Ticket Issuance
- Access Control
- Notifications
- Reporting

Rules:

- Each service owns its data exclusively.
- Never create shared-write database ownership across services.
- Cross-service references are IDs only.
- Cross-service reads must use service APIs or event projections, not direct database reads.
- Reporting is built from read models/projections and must not query other services' databases directly.
- AuditLog is cross-cutting and append-only; sensitive state changes must emit audit entries.

## Implementation Conventions

- Use hexagonal architecture for every backend service:

```text
<feature>/
  domain/
  application/
  infrastructure/
```

- Prefer the more detailed package shape from `docs/standards/spring-shared-foundation-standard.md` when creating real code:

```text
<feature>/
  domain/
    model/
    service/
    port/out/
    exception/
  application/
    port/in/
    service/
  infrastructure/
    adapters/in/rest/
      dto/
      mapper/
    adapters/out/persistence/
      entity/
      mapper/
      repository/
    config/
```

- Domain code must not depend on Spring, JPA, HTTP, messaging, or other infrastructure.
- Controllers must not contain business logic.
- JPA entities are not domain models.
- Request/response DTOs are not domain models.
- Use explicit, testable mappers. Do not rely on implicit runtime mapping for important domain/API boundaries.
- Define transaction boundaries in the application layer unless a documented exception exists.
- Use Bean Validation for structural request validation and domain/application logic for business invariants.
- Use stable structured error responses with trace context.
- Use OpenAPI documentation for every HTTP contract.
- Use UUID primary keys in PostgreSQL.
- Use Flyway for all schema changes under `src/main/resources/db/migration/`.
- Do not add `TODO` or `FIXME` comments in deliverable code.

## Commands

Always discover commands from the files that exist in the repository before running anything:

- Backend: look for `mvnw`, `pom.xml`, `gradlew`, or `build.gradle`.
- Frontend: look for `package.json` and use its scripts.
- Runtime: look for `docker-compose.yml`, `compose.yml`, or documented Compose files.
- Documentation-only areas may not have install, test, lint, or runtime commands.

Prefer repo-provided wrappers and scripts over global tools. Expected commands by documented stack:

- Backend install/build/test, if Maven wrapper exists: `./mvnw clean verify`
- Backend install/build/test, if Gradle wrapper exists: `./gradlew clean build`
- Frontend install, if Angular workspace exists: `npm ci`
- Frontend test: `npm test`
- Frontend lint: `npm run lint`
- Full local stack, when a Compose file is present: `docker compose up --build`
- Full local stack teardown: `docker compose down`

If both Maven and Gradle files are absent for a service, do not create commands silently. Ask or follow the scaffold already present in that service.

## Testing and Definition of Done

- Follow `docs/phases/phase-4/definition-of-done.md`.
- Backend endpoint work requires service-layer unit tests and integration tests with `@SpringBootTest` plus Testcontainers/PostgreSQL.
- Domain events require tests that publish an event, verify consumer processing, and verify the side effect.
- Flyway migrations must be tested against a clean PostgreSQL database.
- Frontend components require rendering and interaction tests plus accessibility basics.
- Critical flows require smoke testing through the documented local runtime environment.
- Do not claim work is complete without running the relevant verification commands. If verification cannot run because tooling is missing or the relevant project is not scaffolded, state that explicitly.

## Git and PR Conventions

- Branch naming: `<developer-name>/<assigned-microservice>`.
- Examples: `alan/identity`, `ivan/orders`, `david/notifications`, `alex/access-control`.
- Main branches:
  - `main`: production-ready releases only.
  - `develop`: integration branch.
  - Developer branches: individual work branches by assigned service.
- PRs must include the story ID, summary of changes, and how to test.
- PRs require at least one approval from someone who does not own the service.
- CI must pass before merge.
- Use squash merge into `develop`.
- Do not push directly to `develop` or `main`.

## Documentation Rules

- Keep docs consistent with `docs/INDEX.md`; update the index when adding important documentation.
- Prefer current docs over archive content.
- Use Spanish for project-facing documentation unless the surrounding file is already English or the user asks otherwise.
- Use exact domain terms from `docs/phases/phase-1/ubiquitous-language-glossary.md`.
- For architecture-impacting changes, add or update an ADR instead of burying the decision in implementation code.
- If a decision is not documented, mark it as pending or ask for clarification. Do not fill gaps with assumptions.

## Important Warnings

- Microservices are a hard requirement, but the docs also acknowledge operational risk. Keep MVP scope strict.
- Observability is mandatory from day 1: structured logs, correlation IDs, traces, health checks, metrics, and deployment/version visibility.
- Seating/Inventory and Orders are high-contention services. Overbooking is a zero-tolerance failure.
- Orders, Payments, and Ticket Issuance must enforce idempotency where documented.
- RabbitMQ consumers must be idempotent and must define retry/DLQ behavior.
- Access Control follows first-scan-wins semantics and has offline sync risks documented in ADR-007.
- No refunds and no transfer/resale are v1 decisions. Do not implement those flows unless the ADRs change.
- Catalog is collapsed into Event Management as a read model. Do not create a separate Catalog service unless a new ADR changes that.
- Pricing is collapsed into Orders. Do not create a separate Pricing service unless a new ADR changes that.
- Docker Compose is the accepted MVP orchestration choice. Do not introduce Kubernetes/Swarm/ECS without a new decision.
- Verify documentation links before relying on them during edits, especially when files have been moved or reorganized.
qw