# Microservicio: Event Management (Gestión de Eventos)

Responsable del ciclo de vida de los eventos, desde su creación en borrador hasta su aprobación y visibilidad pública.

---

## 1. Responsabilidades
- CRUD de Eventos y Venues (Recintos).
- Gestión de Fechas (Dates) de eventos.
- Proceso de aprobación (Submit for Review / Approve / Reject).
- Modelo de lectura de Catálogo Público (Read Model).
  - `GET /v1/catalog/events` (Público).

## 2. Documentación Técnica Específica
- **Casos de Uso:** [UC-EM-01, UC-EM-03, UC-EM-04, UC-EM-05](../docs/phases/phase-2/use-case-catalog.md).
- **Agregados:** [Event, Venue](../docs/phases/phase-1/aggregate-definitions.md).
- **Contrato API:** [Event Management Endpoints](../docs/phases/phase-3/service-contracts.md).
- **Diagrama ER:** [Modelo de Datos de Event Management](../docs/phases/phase-3/er-diagrams/event-management.md).
- **Decisión de Arquitectura:** [Catálogo colapsado como Read Model (ADR-010)](../docs/phases/phase-3/adrs/ADR-010-catalog-collapsed.md).

## 3. Eventos de Dominio
### Genera (Produce):
- `EventCreated`, `EventReleased`, `EventCanceled`.
- `DateAdded`, `DateCanceled`.
- `VenueCreated`.
### Consume:
- `OrganizerApproved` (para habilitar creación de eventos).

---

## 4. Estándares Aplicables
- [Fase 4: Definition of Done](../docs/phases/phase-4/definition-of-done.md).
