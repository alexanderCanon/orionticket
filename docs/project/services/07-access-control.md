# Microservicio: Access Control (Control de Acceso)

Responsable de la validación física en la puerta del evento y la gestión de la sincronización offline.

---

## 1. Responsabilidades
- Validación de QR en tiempo real (SLA < 100ms).
- Registro de intentos de validación (ValidationRecord).
- Sincronización de escaneos realizados sin conexión (Offline Sync).
- Detección de fraude (Regla de First-scan-wins).

## 2. Documentación Técnica Específica
- **Casos de Uso:** [UC-AC-01, UC-AC-02, UC-AC-03](../../phases/phase-2/use-case-catalog.md).
- **Agregado:** [ValidationRecord](../../phases/phase-1/aggregate-definitions.md).
- **Contrato API:** [Access Control Endpoints](../../phases/phase-3/service-contracts.md).
- **Diagrama ER:** [Modelo de Datos de Access Control](../../phases/phase-3/er-diagrams/access-control.md).
- **Flujo Crítico:** [Sincronización Offline](../../phases/phase-2/critical-flows.md).

## 3. Decisiones Críticas
- **ADR-007:** [First-scan-wins para conflictos offline](../../phases/phase-3/adrs/ADR-007-first-scan-wins.md).
- **ADR-013:** [Relación unidireccional con Ticket](../../phases/phase-3/adrs/ADR-013-validation-ticket-one-way.md).

## 4. Eventos de Dominio
### Genera (Produce):
- `ValidationSucceeded`, `ValidationFailed`.
- `ConflictDetected` (alerta de posible fraude/duplicado).
- `ValidatorSynced`.
### Consume:
- `TicketIssued` (para poblar caché local de validación si aplicase).

---

## 5. Estándares Aplicables
- [NFR-005: Latencia de Validación de Puerta](../../phases/phase-3/non-functional-requirements.md).
