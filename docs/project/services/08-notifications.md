# Microservicio: Notifications (Notificaciones)

Responsable del envío de comunicaciones externas (Email, SMS, WhatsApp) a través de múltiples canales.

---

## 1. Responsabilidades
- Envío asíncrono de notificaciones basado en eventos.
- Gestión de plantillas (Templates).
- Lógica de reintentos en caso de fallo del proveedor.

## 2. Documentación Técnica Específica
- **Casos de Uso:** [UC-NO-01](../docs/phases/phase-2/use-case-catalog.md).
- **Agregado:** [Notification](../docs/phases/phase-1/aggregate-definitions.md).
- **Diagrama ER:** [Modelo de Datos de Notifications](../docs/phases/phase-3/er-diagrams/notifications.md).

## 3. Eventos de Dominio
### Genera (Produce):
- `NotificationDispatched`, `NotificationDelivered`, `NotificationFailed`.
### Consume (Casi todos los eventos):
- `UserRegistered`, `ReservationCreated`, `OrderConfirmed`, `TicketIssued`, `EventCanceled`, etc.

---

## 4. Estándares Aplicables
- [Manejo de DLQ (Dead Letter Queues)](../docs/phases/phase-3/adrs/ADR-014-message-broker.md).
