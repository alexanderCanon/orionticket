# Análisis y Especificaciones Técnicas: OrionTicket

Este documento define la base conceptual y funcional para el desarrollo de la plataforma **OrionTicket**, un sistema de misión crítica para la gestión de eventos masivos con alta concurrencia.

---

## 1. Glosario de Dominio (Phase 4: Artifact Generation)

*   **Evento:** Actividad programada (concierto, teatro, etc.) que puede tener inventario tipo *Stock* o *Asiento*.
*   **Inventario Tipo Stock:** Entradas generales donde solo importa la cantidad disponible (ej. "General").
*   **Inventario Tipo Asiento:** Entradas específicas vinculadas a una ubicación única (ej. "Mesa 4, Silla A").
*   **Reserva (Booking):** Bloqueo temporal de un ticket/asiento iniciado por un usuario.
*   **TTL (Time-To-Live):** Tiempo de expiración de una reserva (5 minutos). Si no se confirma el pago, el inventario se libera automáticamente.
*   **Transacción de Pago:** Operación financiera procesada por el *Mock Payment Service*. Puede resultar en: Éxito, Fallo o Inconsistencia Post-Cobro.
*   **Incidencia de Consistencia:** Estado crítico donde el cobro fue exitoso pero la confirmación del ticket falló. Requiere intervención manual.
*   **Outbox Pattern:** Estrategia de persistencia para asegurar que la notificación de éxito/fallo se envíe a la mensajería asíncrona (Kafka/RabbitMQ) solo si la base de datos local se actualizó correctamente.

---

## 2. Requerimientos No Funcionales (NFRs)

| ID | Atributo | Especificación |
| :--- | :--- | :--- |
| **NFR-01** | **Rendimiento** | Soportar ráfagas de hasta 50,000 peticiones concurrentes en el proceso de reserva/pago. |
| **NFR-02** | **Disponibilidad** | Arquitectura distribuida sin punto único de falla (HA). |
| **NFR-03** | **Consistencia** | Garantizar "No Sobreventa" mediante locks lógicos en Redis y transacciones ACID en PostgreSQL. |
| **NFR-04** | **Resiliencia** | El sistema debe recuperarse de caídas de servicios mediante reintentos, colas de mensajes y el patrón SAGA para compensaciones. |
| **NFR-05** | **Observabilidad** | Exposición de métricas (Prometheus/Grafana) para monitorear latencias p95/p99 y tasas de error en tiempo real. |

---

## 3. Mapeo Funcional (Casos de Uso)

### Actor: Comprador
1.  **UC-01: Reservar Entrada (Stock):** Seleccionar cantidad y bloquear stock temporalmente.
2.  **UC-02: Reservar Asiento (Nominal):** Seleccionar ubicación específica y bloquear asiento.
3.  **UC-03: Procesar Pago:** Realizar transacción vía Mock Payment.
4.  **UC-04: Recibir Ticket Digital:** Obtener confirmación y comprobante tras pago exitoso.

### Actor: Administrador
1.  **UC-05: Gestionar Eventos:** Crear, editar y configurar el tipo de inventario (Stock/Asiento).
2.  **UC-06: Dashboard de Ventas:** Visualizar métricas de ingresos e inventario en tiempo real.
3.  **UC-07: Resolución de Incidencias:** Gestionar manualmente casos de "Cobro sin Ticket" (Inconsistencias).

---

## 4. Historias de Usuario (Sprint 1 - MVP)

### US-01: Reserva de Inventario Híbrido
**Como** Comprador, **quiero** que el sistema reserve mi entrada (ya sea stock o asiento específico) por 5 minutos, **para** tener tiempo de completar el pago sin perder mi lugar.
*   **Criterio de Aceptación 1:** El sistema debe descontar del stock o marcar el asiento como "Reservado" inmediatamente.
*   **Criterio de Aceptación 2:** Si el pago no se confirma en 300 segundos (5 min), el inventario debe volver a estar "Disponible".
*   **Criterio de Aceptación 3:** Se debe garantizar idempotencia (un usuario no puede reservar el mismo asiento dos veces simultáneamente).

### US-02: Garantía de Consistencia ante Fallos
**Como** Administrador, **quiero** recibir una alerta en el Dashboard cuando ocurra una falla post-cobro, **para** poder asignar el ticket manualmente y evitar una mala experiencia al cliente.
*   **Criterio de Aceptación 1:** Si el `payment-service` confirma éxito pero el `inventory-service` falla en la confirmación final, se debe crear un registro en la tabla de `Incidencias`.
*   **Criterio de Aceptación 2:** El Dashboard debe mostrar el `TransactionID` y datos del usuario afectado.
*   **Criterio de Aceptación 3:** El Admin debe tener un botón para "Confirmar Manualmente" o "Reembolsar".

### US-03: Alta Concurrencia (Burst Traffic)
**Como** Negocio, **quiero** que el sistema no colapse si 50,000 personas intentan comprar al mismo tiempo, **para** no perder ventas durante lanzamientos masivos.
*   **Criterio de Aceptación 1:** Uso de Redis para gestión de colas de entrada y locks rápidos.
*   **Criterio de Aceptación 2:** Desacoplamiento de servicios mediante Kafka/RabbitMQ para que el proceso de notificación no bloquee el flujo de compra.
