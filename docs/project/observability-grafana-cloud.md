# Observabilidad con Grafana Cloud

Este documento define la base mínima de observabilidad para OrionTicket usando Grafana Cloud como plataforma gestionada.

La intención no es tener dashboards bonitos al final del proyecto. La intención es que cada microservicio pueda explicar qué está pasando cuando algo falla: latencia, errores, trazas, logs, eventos, colas, dependencias y métricas de negocio.

## Decisión Operativa

La observabilidad del MVP se centraliza en Grafana Cloud:

- Métricas: Prometheus/Micrometer.
- Dashboards: Grafana Cloud.
- Logs: logs estructurados enviados a Grafana Cloud Logs.
- Trazas: OpenTelemetry enviado a Grafana Cloud Traces.
- Alertas: Grafana Cloud Alerting.

En local, los servicios deben exponer su telemetría, pero no es obligatorio levantar Grafana ni Prometheus en Docker Compose. La exportación hacia Grafana Cloud debe depender de variables de entorno.

## Arquitectura Mínima

```text
Spring Boot services
  ├─ /actuator/health
  ├─ /actuator/info
  ├─ /actuator/prometheus
  ├─ JSON logs with traceId/correlationId
  └─ OpenTelemetry traces

Collector / Agent
  ├─ scrapes Prometheus metrics
  ├─ collects logs
  ├─ receives or forwards OTLP traces
  └─ sends telemetry to Grafana Cloud

Grafana Cloud
  ├─ Metrics
  ├─ Logs
  ├─ Traces
  ├─ Dashboards
  └─ Alerts
```

El collector puede ser Grafana Alloy, OpenTelemetry Collector u otro agente compatible. La decisión exacta del collector puede cerrarse cuando se prepare el despliegue real.

## Requisitos por Microservicio

Cada servicio Spring Boot debe incluir:

- Spring Boot Actuator.
- Micrometer Prometheus registry.
- endpoint `/actuator/prometheus` habilitado.
- endpoint `/actuator/health` para checks de vida.
- endpoint `/actuator/info` con nombre, versión y entorno.
- logs estructurados en JSON.
- `traceId` y `spanId` visibles en logs.
- propagación de trazas en llamadas HTTP y mensajes RabbitMQ.
- métricas técnicas: latencia, throughput, tasa de error, uso de JVM.
- métricas de negocio específicas del dominio.

## Métricas de Negocio Mínimas

| Servicio | Métricas mínimas |
|---|---|
| Identity | logins exitosos, logins fallidos, registros, fallos de emisión JWT. |
| Event Management | eventos creados, eventos publicados, rechazos de aprobación. |
| Seating / Inventory | reservas creadas, reservas expiradas, fallos de bloqueo, intentos de sobreventa bloqueados. |
| Orders | órdenes creadas, confirmadas, expiradas, conflictos de idempotencia. |
| Payments | pagos iniciados, autorizados, fallidos, webhooks duplicados, reintentos de webhook. |
| Ticket Issuance | tickets emitidos, fallos de emisión, intentos de emisión duplicada. |
| Access Control | validaciones concedidas, denegadas, dobles escaneos, conflictos de sincronización offline. |
| Notifications | notificaciones enviadas, fallidas, reintentadas, mensajes enviados a DLQ. |
| Reporting | retraso de proyecciones, fallos de proyección, fallos de generación de reportes. |

## Alertas Mínimas

El MVP debe tener alertas para:

- cualquier servicio caído;
- error rate HTTP mayor al umbral acordado;
- latencia p95/p99 por encima de los NFR;
- RabbitMQ con mensajes acumulados o DLQ creciendo;
- fallos de pagos o webhooks repetidos;
- intentos de sobreventa bloqueados;
- fallos de emisión de tickets;
- fallos continuos de envío de notificaciones;
- retraso de proyecciones de Reporting mayor a 30 segundos.

## Variables de Entorno

Estas variables deben existir en los archivos `.env.example` y configurarse realmente por entorno cuando se active la exportación cloud:

```dotenv
OBSERVABILITY_ENABLED=false
OTEL_SERVICE_NAME=orionticket-service-name
OTEL_EXPORTER_OTLP_ENDPOINT=https://otlp-gateway-prod.example.grafana.net/otlp
OTEL_EXPORTER_OTLP_HEADERS=Authorization=Basic replace-with-grafana-cloud-token
GRAFANA_CLOUD_PROMETHEUS_REMOTE_WRITE_URL=https://prometheus-prod.example.grafana.net/api/prom/push
GRAFANA_CLOUD_PROMETHEUS_USERNAME=replace-with-instance-id
GRAFANA_CLOUD_API_KEY=replace-with-token
GRAFANA_CLOUD_LOKI_URL=https://logs-prod.example.grafana.net/loki/api/v1/push
GRAFANA_CLOUD_TEMPO_OTLP_ENDPOINT=https://tempo-prod.example.grafana.net/otlp
```

En `local`, `OBSERVABILITY_ENABLED` puede permanecer en `false` hasta que el equipo tenga credenciales compartidas o un entorno de prueba en Grafana Cloud. En `prod`, debe estar en `true`.

## Checklist de Preparación

Antes de considerar listo un microservicio:

- `/actuator/health` responde correctamente.
- `/actuator/prometheus` expone métricas.
- las métricas tienen tags de `service`, `environment` y `version`.
- los logs incluyen `traceId` o `correlationId`.
- las trazas se propagan en llamadas salientes.
- los consumidores RabbitMQ registran éxito, fallo, retry y DLQ.
- existe al menos un panel básico por servicio.
- existen alertas para errores, latencia y caída del servicio.

## Estado Actual

La observabilidad está documentada como requisito obligatorio, pero no debe asumirse implementada hasta verificar cada servicio. Esta guía define la base mínima que debe cerrar el equipo antes de pruebas locales integradas y antes de cualquier despliegue real.
