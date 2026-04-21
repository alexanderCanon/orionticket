# Guía de Coordinación del Equipo: OrionTicket

Este documento define **quién hace qué**, cómo interactuamos y cuáles son los estándares operativos para entregar el proyecto en 45 días.

---

## 1. Responsabilidades y Propiedad (Service Ownership)

Cada integrante es dueño absoluto de sus contextos delimitados de extremo a extremo: Backend (Spring Boot), Base de Datos (Flyway), Tests, Eventos de Dominio y su parte del Frontend (Angular).

| Integrante | Contextos Asignados | Enfoque de Frontend |
| :--- | :--- | :--- |
| **Alan** | [Identidad](services/01-identity.md), [Gestión de Eventos](services/02-event-management.md) | Panel del Organizador (CRUD eventos, Gestión staff), Panel Super Admin. |
| **Ivan** | [Inventario y Asientos](services/03-seating-inventory.md), [Pedidos](services/04-orders.md), [Pagos](services/05-payments.md) | Mapa de asientos interactivo, Flujo de Checkout y pagos. |
| **David** | [Emisión de Boletos](services/06-ticket-issuance.md), [Notificaciones](services/08-notifications.md) | Portal del Comprador (Lista de tickets, visualización de QR). |
| **Alex (Lead)** | [Control de Acceso](services/07-access-control.md), [Reportes](services/09-reporting.md), **DevOps** | App del Validador (Escaneo QR), Dashboards financieros. |

**Responsabilidades de Infraestructura (Alex):**
- Configuración de `docker-compose.yml` raíz.
- Configuración de Spring Cloud Gateway y RabbitMQ central.
- Pipeline de CI/CD (GitHub Actions) y despliegue en VPS.

---

## 2. Estrategia de Git (Branching)

### Nomenclatura de Ramas
`<nombre-del-dev>/<microservicio-asignado>`

- **Ejemplos:** `alan/identity`, `ivan/orders`, `david/notifications`, `alex/access-control`.
- Mantener una rama por desarrollador y microservicio asignado para simplificar la coordinación del equipo.
- Si un desarrollador trabaja en más de un microservicio, debe usar una rama separada por microservicio.

### Modelo de Ramas
- **`main`:** Código listo para producción. Solo lanzamientos etiquetados.
- **`develop`:** Rama de integración. Todos los PRs de características se mezclan aquí.
- **Ramas de desarrollador:** Trabajo individual por microservicio asignado.

---

## 3. Reglas de Pull Request (PR)

1.  **Aprobación Obligatoria:** Todo PR requiere al menos **1 aprobación** de un compañero que NO sea el dueño de ese servicio.
2.  **Descripción Clara:** Debe incluir el ID de la historia de usuario (US-XXX), qué cambió y cómo probarlo.
3.  **CI Verde:** El pipeline de CI debe pasar (compilación, tests, build de Docker) antes de permitir el merge.
4.  **Squash Merge:** Se usará "Squash merge" hacia `develop` para mantener un historial limpio.
5.  **Sin Commits Directos:** Prohibido pushear directamente a `develop` o `main`.

---

## 4. Flujo de Trabajo Diario

### Formato de Standup (15 min máx.)
Se realiza diariamente en horario acordado. Si alguien no puede asistir, debe publicar su reporte de forma asíncrona por WhatsApp antes de la reunión.

**Las 3 preguntas:**
1. ¿Qué terminé desde el último standup? (Referencia a US-XXX).
2. ¿En qué trabajaré hoy?
3. ¿Tengo algún bloqueador? (Indicar nombre de la persona o dependencia).

### Ceremonias de Sprint
- **Sprint Planning (Día 1):** Asignación de historias y clarificación de criterios de aceptación.
- **Daily Standup:** Sincronización y desbloqueo.
- **Sprint Review (Último día):** Demo del software funcionando.
- **Sprint Retrospective:** Qué salió bien y qué mejorar.

---

## 5. Regla de "Contrato Primero" (Contract-First)

> **Ninguna implementación de lógica de negocio comienza hasta que los contratos de API y esquemas de eventos estén publicados.**

1. Antes de codear lógica, los endpoints de `service-contracts.md` y eventos de `event-schemas.md` deben ser aceptados.
2. **Mocks Obligatorios:** Para el **Día 5**, cada servicio debe proveer un Mock funcional (vía endpoint stub o test script) para que sus dependientes puedan trabajar.

---

## 6. Regla de Escalación (2 Horas)

> **Si estás bloqueado por más de 2 horas, escala inmediatamente. No esperes al standup.**

**Protocolo:**
- **0–30 min:** Intenta desbloquearte tú mismo (docs, investigación).
- **30 min–2 h:** Pregunta en el canal de equipo. Etiqueta al dueño del servicio que te bloquea.
- **+2 h:** Contacta directamente a **Alex**.
- **+4 h:** Alex reasigna el trabajo o crea un bypass (mock o stub) para que sigas avanzando.

---

## 7. Referencias Clave
- **Definición de "Hecho" (DoD):** [../phases/phase-4/definition-of-done.md](../phases/phase-4/definition-of-done.md)
- **Backlog de Producto:** [../phases/phase-4/product-backlog.md](../phases/phase-4/product-backlog.md)
- **Guía de Implementación:** [implementation-guide.md](implementation-guide.md)
- **Manuales por Servicio (Técnico):** [services/TECHNICAL_INDEX.md](services/TECHNICAL_INDEX.md)
