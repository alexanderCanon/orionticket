# OrionTicket — Índice de Documentación

> **Este archivo reemplaza el conocimiento trivial. Si no está en el índice, no existe.**

---

## Referencia Rápida

| Pregunta | Respuesta |
|---|---|
| **"¿Qué significa [término] en este sistema?"** | [Glosario de Lenguaje Ubicuo](phases/phase-1/ubiquitous-language-glossary.md) — 21 términos unificados. |
| **"¿Quién es el dueño de estos datos?"** | [Mapa de Pertenencia de Datos](phases/phase-3/data-ownership-map.md) — Definición por agregados. |
| **"¿Qué eventos produce cada servicio?"** | [Mapa de Eventos de Dominio](phases/phase-1/domain-events-map.md). Ver esquemas técnicos en [Esquemas de Eventos (JSON)](phases/phase-3/event-schemas.md). |
| **"¿Cuáles son los criterios de aceptación?"** | [Definición de "Hecho" (DoD)](phases/phase-4/definition-of-done.md) — Checklist de calidad técnica. |
| **"¿Qué debo implementar en este Sprint?"** | [Guía de Implementación](project/implementation-guide.md) — Plan detallado de 45 días. |

---

## Especificaciones del Sistema

> `docs/` — Documentación atemporal, versionada y centralizada.  
> Estos documentos definen **qué** es el sistema y **por qué** se tomaron las decisiones.

---

### Capítulo 0 — Descubrimiento
[Documento de Descubrimiento](phases/phase-0/discovery.md) — Requerimientos de negocio, visión del producto y alcance del MVP.

### Capítulo 1 — Modelado de Dominio
Lenguaje, eventos, contextos y definición de agregados.
- [Glosario de Lenguaje Ubicuo](phases/phase-1/ubiquitous-language-glossary.md)
- [Mapa de Eventos de Dominio](phases/phase-1/domain-events-map.md)
- [Diagramas de Contextos Delimitados](phases/phase-1/bounded-context-diagrams.md)
- [Definiciones de Agregados](phases/phase-1/aggregate-definitions.md)

### Capítulo 2 — Casos de Uso y Flujos
Comportamiento del sistema, actores y reglas de negocio.
- [Mapa de Actores y Roles](phases/phase-2/actor-role-map.md)
- [Catálogo de Casos de Uso](phases/phase-2/use-case-catalog.md)
- [Flujos Críticos (Diagramas de Secuencia)](phases/phase-2/critical-flows.md)
- [Diagramas de Flujo del Sistema](phases/phase-2/system-flow-diagrams.md)
- [Reglas de Negocio](phases/phase-2/business-rules.md)
- [Requerimientos Funcionales (UML)](phases/phase-2/functional-requirements.md)

### Capítulo 3 — Arquitectura
Contratos de API, esquemas, modelos ER y decisiones técnicas.
- [Requerimientos No Funcionales (NFRs)](phases/phase-3/non-functional-requirements.md)
- [Contratos de Servicio (API REST)](phases/phase-3/service-contracts.md)
- [Esquemas de Eventos (JSON Schema)](phases/phase-3/event-schemas.md)
- [Mapa de Pertenencia de Datos (Ownership)](phases/phase-3/data-ownership-map.md)
- [Diagrama de Despliegue e Infraestructura](phases/phase-3/deployment-diagram.md)
- [Carpeta de Decisiones Arquitectónicas (ADRs)](phases/phase-3/adrs/)
  - [ADR-018: Seguridad JWT con JWKS](phases/phase-3/adrs/ADR-018-jwt-jwks-security.md)
  - [ADR-019: Envío de Tickets por Email con Resend](phases/phase-3/adrs/ADR-019-ticket-email-delivery-resend.md)
  - [ADR-020: Observabilidad con Grafana Cloud](phases/phase-3/adrs/ADR-020-observability-grafana-cloud.md)
- [Carpeta de Diagramas Entidad-Relación (ER)](phases/phase-3/er-diagrams/)
  - [Esquema ER Consolidado (Español)](phases/phase-3/er-diagrams/esquema-er-consolidado.md)

### Capítulo 4 — Backlog y Planificación
- [Definición de "Hecho" (DoD)](phases/phase-4/definition-of-done.md)
- [Backlog de Producto (Historias de Usuario)](phases/phase-4/product-backlog.md)

---

### Capítulo 5 — Estándares Técnicos
**Guías de calidad para la construcción de microservicios.**

- **[Carpeta de Estándares](standards/):** Guías de producción y calidad para microservicios y base compartida.

---

---

## 🛠️ Área de Trabajo del Equipo (Project)

> Esta es la carpeta operativa donde el equipo gestiona el día a día.

- **[Guía de Coordinación del Equipo](project/TEAM.md):** Ownership (Alan, Ivan, David, Alex), estrategia de Git y reglas de PR.
- **[Guía de Implementación Técnica](project/implementation-guide.md):** Plan detallado de 45 días.
- **[Observabilidad con Grafana Cloud](project/observability-grafana-cloud.md):** Base mínima para métricas Prometheus, logs, trazas, dashboards y alertas.
- **[Extraccion del API Gateway y laboratorio local](project/gateway-extraction.md):** Compose de microservicios esenciales, RabbitMQ y conexion a PostgreSQL via HAProxy.
- **[Plan de recuperacion de base limpia para laboratorio](project/lab-database-recovery-plan.md):** Pasos para reiniciar pruebas con schemas independientes y Flyway.
- **[Reflexión de Cierre: De Velocidad a Evidencia](project/engineering-reality-check.md):** Cambio de fase para pasar de implementación acelerada a evaluación técnica.
- **[MANUALES POR SERVICIO (Índice Técnico)](project/services/TECHNICAL_INDEX.md):** **Punto de partida para codear.** Documentación fragmentada por servicio y base compartida.
