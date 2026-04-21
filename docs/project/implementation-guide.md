# Guía de Implementación — Estrategia de 45 Días

> **Proyecto:** OrionTicket  
> **Duración:** 45 días hábiles  
> **Equipo:** 4 desarrolladores  
> **Estructura:** 1 semana de setup + 4 Sprints  
> **Stack:** Java 21 + Spring Boot 3, Angular 17+, Docker Compose, RabbitMQ, Spring Cloud Gateway, PostgreSQL, VPS  
> **Ownership de Servicios:** Ver [Guía de Coordinación (TEAM.md)](TEAM.md)

---

## Resumen del Cronograma

| Período | Días | Enfoque |
|---|---|---|
| **Semana de Setup** | Días 1–7 | Infraestructura, contratos, mocks y entorno compartido. |
| **Sprint 1** | Días 8–17 | Bases: Identidad, Gestión de Eventos y Panel Base. |
| **Sprint 2** | Días 18–27 | Transacción Core: Inventario, Pedidos, Pagos y Checkout. |
| **Sprint 3** | Días 28–37 | Ciclo de vida del Boleto: Emisión, Notificaciones y Portal del Comprador. |
| **Sprint 4** | Días 38–45 | Operaciones: Control de Acceso, Reportes, Estabilización física. |

---

## Semana de Setup — Días 1–7 (Equipo Completo)

> **Meta:** Antes de escribir código de producción, la base de infraestructura debe existir y cada desarrollador debe poder levantar el sistema completo localmente con un solo comando.

### Día 1 — Repositorio y Herramientas
- **Todo el equipo:** Clonar repositorio. Leer `INDEX.md`, `TEAM.md` y `Definition of Done`.
- **Alex (Lead):** Crear `docker-compose.yml` raíz: PostgreSQL (9 bases), RabbitMQ (Management UI), Spring Cloud Gateway.
- **Alan:** Configurar tablero de GitHub Projects con 4 columnas de sprint.
- **David:** Configurar reglas de protección de ramas (GitHub).

### Día 2 — Scaffolding de Servicios
- **Alan:** Crear proyectos base para `identity-service` y `event-management-service`.
- **Ivan:** Crear proyectos base para `seating-inventory-service`, `orders-service` y `payments-service`.
- **David:** Crear proyectos base para `ticket-issuance-service` y `notifications-service`.
- **Alex:** Crear proyectos base para `access-control-service` y `reporting-service`.
- **Todos:** Dockerfile (multi-stage), `application.yml` con variables de entorno, Flyway configurado y registro en `docker-compose.yml`.

### Día 3 — Migraciones y Rutas de Gateway
- **Todos:** Escribir migración Flyway V1 para sus servicios usando diagramas ER.
- **Alex (DevOps):** Configurar rutas en el Gateway para los 9 servicios. Filtro de validación JWT (permisivo por ahora).
- **Todos:** Verificar `docker-compose up` y conectividad vía Gateway.

### Día 4 — Infraestructura de Mensajería (RabbitMQ)
- **Alex:** Crear configuración compartida de RabbitMQ: exchanges (topic), colas por consumidor y DLQs.
- **Todos:** Implementar clases base `DomainEventPublisher` y `DomainEventConsumer` usando Spring AMQP.
- **Todos:** Verificar publicación/consumo de un evento de prueba.

### Día 5 — Mocks y Verificación de Contratos
- **Alan:** Publicar Mock de Identidad (generador de JWT de prueba y stub de User).
- **Ivan:** Publicar Mock de Inventario (stub de `/v1/reservations`) y Mock de Pagos (publisher de evento `PaymentAuthorized`).
- **David:** Publicar Mock de Emisión (stub de `/v1/tickets`).
- **Todos:** Cada servicio dependiente verifica que puede consumir los mocks de sus vecinos.

### Día 6 — CI y Observabilidad
- **Alex:** Pipeline de CI en GitHub Actions (compilación y tests en cada PR).
- **Alex:** Configuración de OpenTelemetry (agente Java en Dockerfiles).
- **Todos:** Verificación final de salud del sistema completo (`docker-compose up`).

### Día 7 — Planificación Sprint 1
- **Todos:** Sesión de Sprint Planning (1 hora). Asignar historias US-001 a US-012.
- **Todos:** Revisión de contratos finales (`service-contracts.md` y `event-schemas.md`).

---

## Sprint 1 — Días 8–17: Bases

### Entregables por Persona

#### Alan — Identidad + Gestión de Eventos
- **Días 8–10:** US-001 (Registro y Email único). US-002 (Login JWT y seguridad en Gateway).
- **Días 11–13:** US-003 (Gestión de Roles/Usuarios). US-004 (Creación de Eventos y Fechas).
- **Días 14–17:** US-005 al US-007 (Venues, Aprobación de Eventos). Panel Organizador base (Angular).

#### Ivan — Inventario + Pedidos + Pagos
- **Días 8–10:** Modelo de datos de Inventario, Tandas (Batches) y Pedidos.
- **Días 11–13:** US-013 (Creación de asientos MAPPED/GENERAL). US-014 (Configuración de Tandas/Precios).
- **Días 14–17:** US-015 (API Consulta disponibilidad). Lógica de resolución de precios (Batch + Promos + Fe).

#### David — Emisión + Notificaciones
- **Días 8–11:** Modelo de datos de Tickets. Generador de QR dinámico (2 min TTL).
- **Días 12–14:** Integración con proveedor de Email (Sendgrid/SES). Manejo de colas de reintento.
- **Días 15–17:** US-023 (Consumidor de emisión). US-026 (Notificación de ticket entregado).

#### Alex — Control de Acceso + Reportes + DevOps
- **Días 8–12:** Gateway Hardening (Rate limiting). Modelo de datos de Validaciones y Reportes.
- **Días 13–15:** US-029 (API Validación QR - Mocked).
- **Días 16–17:** Proyecciones base para reportes de ventas y afluencia.

---

## Sprint 2 — Días 18–27: Transacción Core

### Entregables Críticos

#### Ivan — Inventario y Gestión de Reservas (RUTA CRÍTICA ⚠️)
- **Días 18–21:** **US-016: Reserva con bloqueo pesimista.** (Atomicidad en base de datos). **Sin esto, Pedidos no puede avanzar.**
- **Días 22–24:** US-017 (Job de expiración de reservas).
- **Días 25–27:** Load tests de concurrencia: 100 usuarios compitiendo por el mismo asiento.

#### Ivan — Pedidos y Pagos (RUTA CRÍTICA ⚠️)
- **Días 18–21:** **US-018: Creación de Pedido** (Vinculado a reserva real).
- **Días 22–25:** **US-019: Procesamiento de Pago.** Integración con pasarela real/mock. Evento `PaymentAuthorized`.
- **Días 26–27:** Frontend de Checkout: Selección de asiento → Pedido → Pago → Confirmación.

#### Alan — Estabilización y soporte a Transacciones
- **Días 18–22:** US-008 (Cancelación de eventos con liberación de reservas vía eventos).
- **Días 23–27:** Soporte en integración de seguridad y Gateway.

#### David — Emisión Real
- **Días 18–23:** Integración con evento de pago real. Emisión automática de boletos en base a pagos confirmados.

---

## Sprint 3 — Días 28–37: Ciclo de Vida + Portal

#### David — Portal del Comprador
- **Días 28–33:** US-027 (Portal Angular compradores: lista de tickets, visualización de QR dinámico, descarga PDF).
- **Días 34–37:** Refinamiento de notificaciones (Templates: ticket entregado, reserva expirada).

#### Alan & Ivan — Paneles de Gestión
- **Días 28–33:** Panel Organizador: Configuración de mapas de asientos, gestión de tandas de precio, lista de pedidos.
- **Días 34–37:** Panel Super Admin: Workflow de aprobación final, gestión de usuarios de plataforma.

#### Alex — Control de Acceso Real
- **Días 28–33:** Integración de Validación de QR con el servicio de Emisión real. Latencia < 100ms.
- **Días 34–37:** US-031 (Sincronización Offline) y detección de fraude (First-scan-wins).

---

## Sprint 4 — Días 38–45: Operaciones y Estabilización

- **Alex:** US-032 (App del Validador Angular: escaneo cámara, feedback visual GANTED/DENIED). US-033-035 (Reportes finales).
- **Todos:** US-037 (Demo de integración final). Pruebas de punta a punta.
- **Alex:** Preparar entorno de Demo en VPS. Seed data final.

---

## Ruta Crítica (Bloqueadores)

1.  **Día 10:** Seguridad JWT en Gateway (Alan/Alex) -> Bloquea a todos para usar entornos protegidos.
2.  **Día 20:** Endpoint de Reservas con Lock (Ivan) -> Bloquea a Pedidos.
3.  **Día 23:** Evento `PaymentAuthorized` (Ivan) -> Bloquea la Emisión de Boletos.
4.  **Día 39:** Validación de QR integrada (Alex) -> Necesario para demostrar el flujo de entrada.

---

## Escenario de Demo MVP (Día 45)

Para que el proyecto se considere exitoso, el flujo debe funcionar completo:
1. Super Admin crea Organizador.
2. Organizador crea Evento -> Crea Tandas -> Configura Asientos -> Solicita Aprobación.
3. El sistema aprueba (Automático/Manual).
4. El Comprador selecciona asiento -> Crea reserva -> Paga.
5. El sistema emite ticket -> Notifica por Email.
6. El Comprador ve su QR dinámico en el Portal.
7. El Validador escanea el QR -> Acceso concedido -> Ticket marcado como USADO.

---

## Tabla de Riesgos

| Riesgo | Severidad | Mitigación |
|---|---|---|
| **Sobreventa por concurrencia** | Crítica | Bloqueo pesimista en base de datos. Pruebas de carga en Sprint 2. |
| **Retraso en Pasarela de Pago** | Alta | Tener el Mock de pago listo el Día 5. El flujo de boletos no debe detenerse por una integración externa. |
| **Retraso en Seguridad/Auth** | Alta | Uso de JWT de prueba (Mock) hasta que el servicio esté al 100%. |
| **Falla en Validación Offline** | Media | Es la primera funcionalidad en sacrificarse si vamos retrasados. La validación online es prioridad. |
