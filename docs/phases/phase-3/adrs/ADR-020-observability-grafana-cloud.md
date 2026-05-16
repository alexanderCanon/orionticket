# ADR-020: Observability with Grafana Cloud

| Field | Value |
|---|---|
| **Date** | 2026-05-15 |
| **Status** | PROPOSED |

## Context

OrionTicket is a distributed system with multiple Spring Boot microservices, RabbitMQ, PostgreSQL databases, an API Gateway, external payment providers, and external notification providers. The discovery and non-functional requirements already define observability as mandatory from day 1.

The MVP needs operational visibility without forcing the team to operate a full self-hosted monitoring stack. Local development can expose metrics and logs, but production should centralize telemetry in a managed platform.

## Decision

Use **Grafana Cloud** as the managed observability platform for the MVP.

Each Spring Boot microservice must expose Prometheus-compatible metrics through Spring Boot Actuator and Micrometer. Telemetry is collected by an agent or collector and sent to Grafana Cloud.

## Target Components

| Concern | Target |
|---|---|
| Metrics | Prometheus-compatible metrics exported from `/actuator/prometheus` and sent to Grafana Cloud Metrics. |
| Dashboards | Grafana Cloud dashboards. |
| Logs | Structured JSON logs collected and sent to Grafana Cloud Logs. |
| Traces | OpenTelemetry traces sent to Grafana Cloud Traces. |
| Alerts | Grafana Cloud Alerting. |
| Local development | Services expose telemetry locally; cloud export can be disabled by environment variable. |

## Service Requirements

Every microservice must provide:

- `spring-boot-starter-actuator`.
- Micrometer Prometheus registry.
- `/actuator/health`, `/actuator/info`, and `/actuator/prometheus`.
- structured JSON logs with correlation or trace IDs.
- service name, environment, version, and instance metadata.
- OpenTelemetry trace propagation across HTTP and RabbitMQ boundaries.
- business metrics for its critical domain actions.

## Minimum Business Metrics

| Service | Required MVP Metrics |
|---|---|
| Identity | login attempts, login failures, registrations, token issuance failures. |
| Event Management | events created, events published, approval failures. |
| Seating / Inventory | reservations created, reservations expired, seat hold failures, overbooking guard violations. |
| Orders | orders created, orders confirmed, orders expired, idempotency conflicts. |
| Payments | payment attempts, authorized payments, failed payments, webhook retries, duplicate webhook events. |
| Ticket Issuance | tickets issued, issuance failures, duplicate issuance attempts. |
| Access Control | validations granted, validations denied, duplicate scan attempts, offline sync conflicts. |
| Notifications | notifications dispatched, delivery failures, retry attempts, DLQ messages. |
| Reporting | projection lag, projection failures, report generation failures. |

## Required Configuration

| Variable | Purpose |
|---|---|
| `OBSERVABILITY_ENABLED` | Enables cloud telemetry export. |
| `OTEL_SERVICE_NAME` | Logical service name. |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | OTLP endpoint used for traces and optionally metrics/logs. |
| `OTEL_EXPORTER_OTLP_HEADERS` | Authentication headers for Grafana Cloud OTLP ingestion. |
| `GRAFANA_CLOUD_PROMETHEUS_REMOTE_WRITE_URL` | Remote write endpoint for metrics. |
| `GRAFANA_CLOUD_PROMETHEUS_USERNAME` | Grafana Cloud Metrics username or instance ID. |
| `GRAFANA_CLOUD_API_KEY` | Token used by the collector or agent. |
| `GRAFANA_CLOUD_LOKI_URL` | Logs ingestion endpoint. |
| `GRAFANA_CLOUD_TEMPO_OTLP_ENDPOINT` | Trace ingestion endpoint when configured separately. |

## Consequences

- The team does not need to operate Grafana, Prometheus, Loki, and Tempo as production infrastructure for the MVP.
- The local Docker Compose stack does not need to include persistent Grafana or Prometheus by default.
- A lightweight collector or agent becomes part of the deployment topology.
- Telemetry configuration must be environment-driven and disabled safely for local development when credentials are not available.
- Dashboards and alerts become part of the production readiness checklist, not optional polish.

## Non-Goals for MVP

- Self-hosted long-term metrics storage.
- Full SRE-grade incident automation.
- Per-tenant customer-facing observability.
- Advanced anomaly detection.
- Distributed profiling.

## Related Documents

- [Non-Functional Requirements](../non-functional-requirements.md)
- [Deployment Diagram](../deployment-diagram.md)
- [Spring Shared Foundation Standard](../../../standards/spring-shared-foundation-standard.md)
- [Spring Microservices Production Guide](../../../standards/spring-microservices-production-guide.md)
- [Grafana Cloud Observability Guide](../../../project/observability-grafana-cloud.md)
