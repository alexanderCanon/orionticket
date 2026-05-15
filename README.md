# OrionTicket

OrionTicket is a white-label, multi-tenant ticket sales platform designed for large-scale events. It gives event organizers their own branded operating space while keeping the platform in control of identity, sales, ticket issuance, access validation, reporting, and auditability.

The product is being built for the Guatemalan market first, with a strict MVP scope and a strong technical foundation from the beginning. Its core challenge is not only selling tickets, but doing it reliably under high demand: seat reservations must be consistent, payments must be traceable, tickets must not be issued twice, and access control must prevent duplicate QR validation at the door.

## Product Focus

OrionTicket supports organizers that need to publish events, manage venues and seating, define sales batches and promotions, process orders, issue digital tickets, and validate access in real time. The platform is designed for both assigned seating and general admission events.

The v1 product intentionally avoids refund, resale, and transfer flows. Tickets belong to the original buyer until the event, and the system prioritizes operational control, fraud reduction, and predictable event-day validation.

## Architecture

OrionTicket follows a microservices architecture from day one. This is a documented client constraint and an accepted architectural decision, not an incidental implementation detail.

Each service owns its data independently. Cross-service communication happens through service APIs and asynchronous domain events, not shared database access. PostgreSQL is used per service, Flyway manages schema migrations, and RabbitMQ is the backbone for asynchronous messaging.

Backend services follow hexagonal architecture:

```text
<feature>/
  domain/
  application/
  infrastructure/
```

The domain layer must remain independent from Spring, JPA, HTTP, messaging, and other infrastructure concerns. Controllers, DTOs, persistence entities, and message adapters live outside the domain model.

## Service Map

The documented platform boundaries are:

- Identity
- Event Management, including the Catalog read model
- Seating / Inventory
- Orders
- Payments
- Ticket Issuance
- Access Control
- Notifications
- Reporting

The repository currently contains backend service scaffolding and implementation work for several of these boundaries, including identity, event management, access control, ticket issuance, notifications, and reporting. The complete service contracts, ownership rules, event schemas, and implementation standards are maintained under `docs/`.

## Technology Stack

- Java 21
- Spring Boot 3
- Spring Security
- Spring Boot Actuator
- OpenAPI / Swagger
- PostgreSQL
- Flyway
- RabbitMQ
- Docker Compose
- Testcontainers
- OpenTelemetry and structured logging
- Angular 17+ for frontend work defined in the project plan

## Repository Structure

```text
.
|-- docs/                       Project documentation and architectural source of truth
|-- identity-service/           Identity, users, authentication, and roles
|-- event-management-service/   Events, venues, approval, and catalog read model
|-- access-control-service/     Door validation and first-scan-wins access control
|-- ticket-issuance-service/    Ticket generation, QR lifecycle, and ticket status
|-- notifications-service/      Notification records, delivery channels, retries, and DLQ
|-- reporting-service/          Reporting projections and business intelligence views
`-- docker-compose.yml          Local infrastructure and service composition
```

## Documentation

The documentation is the source of truth for the project. If a behavior, field, service boundary, event, or architectural decision is not documented, it should be treated as undefined.

Key entry points:

- `docs/INDEX.md` - central documentation index
- `docs/project/services/TECHNICAL_INDEX.md` - technical index by service
- `docs/project/services/shared-foundation.md` - shared standards for all services
- `docs/standards/` - Spring and microservices implementation standards
- `docs/phases/phase-3/service-contracts.md` - REST service contracts
- `docs/phases/phase-3/event-schemas.md` - domain event schemas
- `docs/phases/phase-4/definition-of-done.md` - delivery and quality checklist

## Local Development

The repository uses service-level Maven projects and Docker Compose for local infrastructure.

To start the root local stack:

```bash
docker compose up --build
```

To stop it:

```bash
docker compose down
```

To run tests for a service that includes the Maven wrapper:

```bash
cd identity-service
./mvnw test
```

For services without a local wrapper, use the Maven command available in that service only after confirming its `pom.xml` and local setup.

## Engineering Standards

OrionTicket is built around several non-negotiable engineering rules:

- Each service owns its database and data model.
- Cross-service references use IDs, not shared relational ownership.
- Flyway migrations are required for schema changes.
- Domain events must be explicit, versioned, and tested.
- RabbitMQ consumers must be idempotent and define retry and DLQ behavior.
- Sensitive state changes must emit audit entries.
- Observability is required from day one: health checks, metrics, structured logs, correlation IDs, and traces.
- High-contention flows such as reservations, orders, payments, ticket issuance, and QR validation require strong idempotency and concurrency control.

## MVP Direction

The MVP is centered on the complete event purchase and access flow:

1. A platform administrator creates an organizer.
2. The organizer creates an event, venue, batches, prices, and seating configuration.
3. The platform approves the event for publication.
4. A buyer selects a seat or ticket type and creates a reservation.
5. The buyer pays for the order.
6. The system issues the ticket and notifies the buyer.
7. The buyer presents a dynamic QR code.
8. Door staff validate the ticket using first-scan-wins semantics.
9. Reporting projections expose operational and sales data.

This repository is organized to keep that flow explicit, testable, and consistent with the documented bounded contexts.
