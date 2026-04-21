# ÍNDICE TÉCNICO: OrionTicket

Este es el punto de entrada para desarrolladores. Aquí encontrarás la documentación fragmentada por microservicio y los estándares transversales de construcción.

---

## 🚀 Punto de Partida Obligatorio
- **[Base Compartida y Estándares Transversales](shared-foundation.md):** Lee esto antes de tocar una línea de código. Contiene las reglas de arquitectura hexagonal que aplican a todos los servicios.

---

## 📦 Microservicios (Manual por Servicio)
Cada desarrollador debe seguir el documento de su servicio asignado:

1.  **[01. Identidad](01-identity.md):** Seguridad, usuarios y roles.
2.  **[02. Gestión de Eventos](02-event-management.md):** CRUD de eventos, recintos y aprobación.
3.  **[03. Inventario y Asientos](03-seating-inventory.md):** Bloqueos atómicos, tandas y concurrencia.
4.  **[04. Pedidos](04-orders.md):** Checkout, cálculos de precio y promociones.
5.  **[05. Pagos](05-payments.md):** Pasarelas de pago y liquidaciones a organizadores.
6.  **[06. Emisión de Boletos](06-ticket-issuance.md):** Generación de boletos y QR dinámico.
7.  **[07. Control de Acceso](07-access-control.md):** Validación en puerta y modo offline.
8.  **[08. Notificaciones](08-notifications.md):** Email, SMS y WhatsApp.
9.  **[09. Reportes](09-reporting.md):** Proyecciones y Business Intelligence.

---

## 🛠️ Herramientas de Gestión
- **[Plan de Implementación (45 días)](../implementation-guide.md):** Cronograma y entregables.
- **[Definición de "Hecho" (DoD)](../../phases/phase-4/definition-of-done.md):** Checklist de calidad para cada tarea.
- **[Guía de Coordinación de Equipo](../TEAM.md):** Asignaciones, ramas de Git y reglas de revisión.

---

## 📚 Referencia Completa (Fases)
La documentación original por fases sigue disponible para consultas profundas:
- [Fase 0: Descubrimiento](../../phases/phase-0/discovery.md)
- [Fase 1: Dominio](../../phases/phase-1/aggregate-definitions.md)
- [Fase 2: Casos de Uso](../../phases/phase-2/use-case-catalog.md)
- [Fase 3: Arquitectura](../../phases/phase-3/service-contracts.md)
- [Fase 4: Planificación](../../phases/phase-4/product-backlog.md)
