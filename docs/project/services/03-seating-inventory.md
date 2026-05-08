# Microservicio: Seating / Inventory (Inventario y Asientos)

Responsable de la alta disponibilidad de asientos y la gestión de la concurrencia durante la reserva. Es el componente más crítico del sistema.

---

## 1. Responsabilidades
- Configuración de Seating Maps (Zonas, Filas, Asientos).
- Gestión de Tandas (Batches) y precios.
- **Reserva de Asientos (Atomic Lock):** Garantizar que un asiento solo se reserve una vez bajo alta carga.
- Expiración de reservas (Cleanup job).

## 2. Documentación Técnica Específica
- **Casos de Uso:** [UC-SI-01, UC-SI-02, UC-SI-03](../../phases/phase-2/use-case-catalog.md).
- **Agregados:** [Seat (con Batch y Reservation)](../../phases/phase-1/aggregate-definitions.md).
- **Contrato API:** [Seating/Inventory Endpoints](../../phases/phase-3/service-contracts.md).
- **Diagrama ER:** [Modelo de Datos de Seating/Inventory](../../phases/phase-3/er-diagrams/seating-inventory.md).
- **Flujo Crítico:** [Secuencia de Reserva y Expiración](../../phases/phase-2/critical-flows.md).

## 3. Decisiones Críticas
- **ADR-004:** [Modelo de Reserva Universal](../../phases/phase-3/adrs/ADR-004-universal-reservation.md).
- **ADR-005:** [Consistencia Atómica en Tandas/Reservas](../../phases/phase-3/adrs/ADR-005-atomic-batch-reservation.md).

## 4. Eventos de Dominio
### Genera (Produce):
- `ReservationCreated`, `ReservationExpired`, `ReservationReleased`.
- `BatchCreated`, `BatchActivated`, `BatchExhausted`.
### Consume:
- `DateAdded` (para inicializar inventario).
- `PaymentFailed` (para liberar reserva).

---

## 5. Estándares Aplicables
- [NFR-017 / NFR-024: Concurrencia y Bloqueos](../../phases/phase-3/non-functional-requirements.md).
