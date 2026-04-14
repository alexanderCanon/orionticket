# Definition of Done

> **Phase:** 4 — Backlog & Planning  
> **Stack:** Java 21 + Spring Boot 3, Angular 17+, Docker Compose, RabbitMQ, Spring Cloud Gateway, VPS  
> **Rule:** If any criterion is not met, the item is not done. No exceptions.

---

## 1. User Story

A user story is **done** when ALL of the following are true:

- [ ] All acceptance criteria listed in the story are implemented and verified.
- [ ] Backend endpoint(s) pass all criteria in §2 below.
- [ ] Frontend component(s) pass all criteria in §3 below.
- [ ] Domain event(s) pass all criteria in §4 below.
- [ ] Integration test covers the happy path end-to-end (API → service → RabbitMQ → consumer).
- [ ] Manual smoke test executed by the developer on `docker-compose up` local environment.
- [ ] No `// TODO` or `// FIXME` comments remain in the delivered code.
- [ ] PR approved by at least one other team member.
- [ ] PR merged to `develop` branch. CI pipeline passes.
- [ ] AuditLog entry emitted for every sensitive state change (per business-rules.md BR-CC-01).

---

## 2. Backend Service Endpoint (Spring Boot)

An endpoint is **done** when ALL of the following are true:

- [ ] Controller, service, and repository layers implemented.
- [ ] Request validation with `@Valid` / Bean Validation annotations. Invalid input returns `422` with structured error body.
- [ ] Response matches the schema defined in `docs/phases/phase-3/service-contracts.md` exactly — same field names, same types, same HTTP status codes.
- [ ] Error codes match the error table in `service-contracts.md` for this endpoint.
- [ ] Authentication enforced via Spring Security. Unauthenticated requests return `401`.
- [ ] Authorization enforced per role. Unauthorized requests return `403`. Roles match `docs/phases/phase-2/actor-role-map.md`.
- [ ] Unit tests for service layer logic — minimum 80% line coverage on the service class.
- [ ] Integration test with `@SpringBootTest` + TestContainers (PostgreSQL) for the full request/response cycle.
- [ ] Idempotency key validated on Orders, Payments, and Ticket Issuance endpoints (per ADR-008).
- [ ] Structured JSON log emitted on entry and exit (correlation ID propagated via `X-Correlation-Id` header).
- [ ] OpenTelemetry span created for the endpoint. Trace ID visible in logs.
- [ ] Endpoint documented in Swagger/OpenAPI (`springdoc-openapi`). Auto-generated docs match `service-contracts.md`.
- [ ] Health check endpoint (`/actuator/health`) returns `UP`.
- [ ] Dockerfile builds successfully. Container starts and responds on `docker-compose up`.

---

## 3. Frontend Component (Angular)

A component is **done** when ALL of the following are true:

- [ ] Component renders correctly on Chrome, Firefox, and Safari (latest).
- [ ] Responsive layout tested at 360px (mobile), 768px (tablet), and 1440px (desktop).
- [ ] All interactive elements have unique HTML `id` attributes (for browser testing).
- [ ] Form validation matches backend validation — same required fields, same constraints.
- [ ] Loading states displayed during API calls (spinner or skeleton).
- [ ] Error states displayed when API returns 4xx or 5xx (user-facing message, not raw JSON).
- [ ] API calls use Angular `HttpClient` with proper error handling (`catchError`).
- [ ] Auth token attached via HTTP interceptor. Unauthorized redirects to login.
- [ ] No `console.log` or `console.error` in production code.
- [ ] Component test (Jasmine/Karma or Jest) covers rendering and user interaction.
- [ ] Accessibility: all form inputs have `<label>`, all images have `alt`, all buttons have visible text or `aria-label`.

---

## 4. Domain Event (RabbitMQ)

A domain event is **done** when ALL of the following are true:

- [ ] Event payload matches the JSON schema defined in `docs/phases/phase-3/event-schemas.md` exactly — same field names, same types.
- [ ] Event published to RabbitMQ exchange with correct routing key.
- [ ] Event includes `eventId` (UUID), `eventType` (string), and `occurredAt` (ISO-8601 UTC).
- [ ] Publisher uses Spring AMQP `RabbitTemplate` with JSON message converter.
- [ ] Consumer(s) registered and processing the event. Consumer idempotency verified (duplicate eventId is ignored, not re-processed).
- [ ] Dead-letter exchange (DLX) configured for the queue. Failed messages routed to DLQ after 3 retries.
- [ ] Integration test: publish event → verify consumer processes it → verify side effect (DB state or downstream event).
- [ ] Event logged with correlation ID (same trace as the originating HTTP request).
- [ ] Consumer failure does not crash the service — exception caught, logged, message nacked to DLQ.

---

## 5. ER Diagram Implementation (Database)

An ER diagram implementation is **done** when ALL of the following are true:

- [ ] Database schema matches the ER diagram in `docs/phases/phase-3/er-diagrams/{context}.md` — same entities, same fields, same types.
- [ ] Flyway migration script created in `src/main/resources/db/migration/` with sequential version number.
- [ ] Primary keys are UUIDs (`uuid` type in PostgreSQL).
- [ ] Cross-service references stored as `uuid` columns — no foreign key constraints across service boundaries (per data-ownership-map.md).
- [ ] `NOT NULL` constraints match required fields in aggregate definitions.
- [ ] Unique constraints match `UK` designations in ER diagrams.
- [ ] Indexes created for columns used in `WHERE` clauses on high-frequency queries (seatId + dateId for inventory lookups, buyerId for order lookups).
- [ ] Migration runs successfully on a clean database (`docker-compose up` from scratch).
- [ ] Migration is idempotent — running it twice does not fail.
- [ ] JPA entities (`@Entity`) map correctly to the schema. `@Column` names match migration.

---

## 6. Full Service Deployment

A service deployment is **done** when ALL of the following are true:

- [ ] `Dockerfile` uses multi-stage build: build with Maven/Gradle → run with JRE slim image.
- [ ] Service defined in root `docker-compose.yml` with: image, ports, environment variables, health check, restart policy (`unless-stopped`), depends_on (with health check condition).
- [ ] Environment variables externalized — no hardcoded credentials, URLs, or ports in code. All config via `application.yml` + `${ENV_VAR}` placeholders.
- [ ] Service connects to its own PostgreSQL database container (one database per service — per data-ownership-map.md).
- [ ] Service connects to shared RabbitMQ container.
- [ ] Spring Cloud Gateway route configured for this service in the gateway `application.yml`.
- [ ] `docker-compose up` from repo root starts all services including this one without manual intervention.
- [ ] Health check passes within 60 seconds of container start.
- [ ] Structured JSON logs visible in `docker-compose logs {service-name}`.
- [ ] OpenTelemetry traces exported to the observability stack.
- [ ] No port conflicts with other services in the Compose file.
- [ ] Service responds correctly to at least one endpoint via the gateway URL (e.g., `http://localhost:8080/v1/{service}/health`).
