# Esquema de Diagramas ER Consolidado — OrionTicket

Este documento consolida y unifica el modelo de datos de todos los microservicios de OrionTicket.

## 📌 Principios de Arquitectura de Datos

De acuerdo con el **ADR-001 (Arquitectura de Microservicios)**, el **ADR-008 (Base de Datos Compartida y Multi-Esquema para Desarrollo/MVP)** y los estándares del proyecto:
1. **Aislamiento de Datos por Servicio**: Cada microservicio tiene la propiedad exclusiva de sus datos. No existen llaves foráneas (`FK`) a nivel de base de datos entre tablas de diferentes microservicios.
2. **Referencias Cruzadas por ID**: Las relaciones entre microservicios se realizan únicamente mediante identificadores (`UUID`) lógicos.
3. **Eventos de Dominio y Consistencia Eventual**: La propagación de estados y la actualización de modelos de lectura (como el servicio de Reportes) se realiza de forma asíncrona consumiendo eventos de RabbitMQ.
4. **Modelos Colapsados**: 
   - El catálogo de lectura está colapsado dentro de **Event Management** (`ADR-010`).
   - La resolución de precios y promociones está colapsada dentro de **Orders** (`ADR-011`).

---

## 🗺️ Mapa General de Referencias entre Servicios

```mermaid
graph TD
    AccessControl[Control de Acceso] -->|ticketId| TicketIssuance[Emisión de Tickets]
    AccessControl -->|eventId, dateId| EventManagement[Gestión de Eventos]
    
    Orders[Pedidos] -->|buyerId| Identity[Identidad]
    Orders -->|eventId, dateId| EventManagement
    Orders -->|reservationId, seatId| SeatingInventory[Inventario y Asientos]
    
    Payments[Pagos] -->|orderId| Orders
    Payments -->|buyerId, organizerId| Identity
    Payments -->|eventId, dateId| EventManagement
    
    TicketIssuance -->|orderId| Orders
    TicketIssuance -->|buyerId| Identity
    TicketIssuance -->|eventId, dateId| EventManagement
    TicketIssuance -->|seatId| SeatingInventory
    
    SeatingInventory -->|eventId, dateId| EventManagement
    SeatingInventory -->|buyerId| Identity
    
    Notifications[Notificaciones] -->|recipientId| Identity
    
    Reporting[Reportes] -.->|Proyecciones asíncronas de eventos| Identity
    Reporting -.->|Proyecciones asíncronas de eventos| EventManagement
```

---

## 1. Identidad (Identity)
* **Contexto Delimitado**: Identity
* **Propietario**: Identity Service
* **Propósito**: Gestiona usuarios, roles y permisos de la plataforma.

```mermaid
erDiagram
    USER {
        uuid userId PK
        string email UK
        string passwordHash
        string fullName
        string phone
        string status "ACTIVE | SUSPENDED | UNVERIFIED"
        uuid roleId FK
        uuid organizerId "null para usuarios nivel plataforma"
        datetime createdAt
    }

    ROLE {
        uuid roleId PK
        string name UK
    }

    PERMISSION {
        uuid permissionId PK
        uuid roleId FK
        string permission
    }

    ROLE ||--o{ PERMISSION : "tiene"
    ROLE ||--o{ USER : "asignado a"
```

### Descripción de Entidades
* **USER (Usuario)**: Información principal del usuario.
  * `status`: Define si la cuenta está activa, suspendida o sin verificar.
  * `organizerId`: Se refiere al ID del propio usuario si actúa como Organizador.
* **ROLE (Rol)**: Roles del sistema (ej. CLIENTE, ORGANIZADOR, ADMIN).
* **PERMISSION (Permiso)**: Permisos granulares asociados a cada rol.

---

## 2. Gestión de Eventos (Event Management)
* **Contexto Delimitado**: Event Management
* **Propietario**: Event Management Service
* **Propósito**: Gestiona los eventos, las fechas y los recintos de presentación. El catálogo de consulta rápida está colapsado dentro de este servicio (`ADR-010`).

```mermaid
erDiagram
    EVENT {
        uuid eventId PK
        uuid organizerId "ref: Identity.userId"
        string name
        string description
        string category
        string status "DRAFT | UNDER_REVIEW | RELEASED | CANCELED"
        string rejectionReason "null salvo si es rechazado"
        datetime createdAt
        datetime updatedAt
    }

    DATE {
        uuid dateId PK
        uuid eventId FK
        datetime scheduledAt
        uuid venueId FK
        integer capacity
        string status "ACTIVE | CANCELED"
    }

    VENUE {
        uuid venueId PK
        uuid organizerId "ref: Identity.userId"
        string name
        string address
        integer totalCapacity
    }

    EVENT ||--o{ DATE : "tiene"
    VENUE ||--o{ DATE : "alberga"
```

### Descripción de Entidades
* **EVENT (Evento)**: Información general del espectáculo musical o deportivo.
* **DATE (Fecha)**: Instancia de programación temporal de un evento en un recinto específico.
* **VENUE (Recinto/Estadio)**: El lugar físico donde se lleva a cabo el evento.

---

## 3. Asientos e Inventario (Seating / Inventory)
* **Contexto Delimitado**: Seating / Inventory
* **Propietario**: Seating/Inventory Service
* **Propósito**: Control estricto de disponibilidad física y lógica de asientos, zonas y lotes. Bloqueos temporales para evitar sobreventa (Overbooking - Tolerancia Cero).

```mermaid
erDiagram
    SEAT {
        uuid seatId PK
        uuid eventId "ref: EventManagement.eventId"
        uuid dateId "ref: EventManagement.dateId"
        string zone "null para GENERAL_ADMISSION"
        string section "null para GENERAL_ADMISSION"
        string row "null para GENERAL_ADMISSION"
        string type "MAPPED | GENERAL_ADMISSION"
        string status "AVAILABLE | RESERVED | SOLD | BLOCKED"
        string accessPolicy
        uuid batchId FK
    }

    RESERVATION {
        uuid reservationId PK
        uuid seatId FK
        uuid buyerId "ref: Identity.userId"
        datetime expiresAt
        string status "ACTIVE | EXPIRED | RELEASED"
    }

    BATCH {
        uuid batchId PK
        uuid eventId "ref: EventManagement.eventId"
        uuid dateId "ref: EventManagement.dateId"
        string name
        decimal price
        string currency
        integer capacity
        integer sold "incrementado atómicamente con la Reserva"
        string status "SCHEDULED | ACTIVE | EXHAUSTED | EXPIRED"
        datetime scheduledStartAt
    }

    SEAT ||--o| RESERVATION : "mantiene"
    BATCH ||--o{ SEAT : "asigna precio a"
```

### Descripción de Entidades
* **SEAT (Asiento)**: Representa el inventario de la sala o estadio. Puede ser numerado (`MAPPED`) o de admisión general (`GENERAL_ADMISSION`).
* **RESERVATION (Reserva Temporal)**: Bloqueo de un asiento durante la compra por un tiempo determinado (`expiresAt`).
* **BATCH (Lote de Venta/Fase)**: Define las etapas de precios de las entradas (ej. Preventa, Fase 1, Fase 2) con capacidades limitadas.

---

## 4. Pedidos (Orders)
* **Contexto Delimitado**: Orders
* **Propietario**: Orders Service
* **Propósito**: Registro de transacciones de compra, aplicación de promociones y desglose financiero.

```mermaid
erDiagram
    ORDER_TABLE {
        uuid orderId PK
        uuid buyerId "ref: Identity.userId"
        uuid eventId "ref: EventManagement.eventId"
        uuid dateId "ref: EventManagement.dateId"
        uuid reservationId "ref: SeatingInventory.reservationId"
        string status "CREATED | PAYMENT_INITIATED | CONFIRMED | EXPIRED | FAILED"
        decimal subtotal
        uuid promotionId "ref: PROMOTION.promotionId, nullable"
        decimal promotionDiscount
        decimal serviceFee
        decimal total
        string currency
        datetime createdAt
    }

    LINE_ITEM {
        uuid lineItemId PK
        uuid orderId FK
        uuid seatId "ref: SeatingInventory.seatId"
        decimal batchPrice
        integer quantity
    }

    PROMOTION {
        uuid promotionId PK
        uuid eventId "ref: EventManagement.eventId"
        string code
        string discountType "PERCENTAGE | FIXED"
        decimal discountValue
        integer maxUses
        integer usedCount
        string status "CREATED | ACTIVE | DEACTIVATED | EXHAUSTED"
    }

    ORDER_TABLE ||--o{ LINE_ITEM : "contiene"
    PROMOTION ||--o{ ORDER_TABLE : "aplicado a"
```

### Descripción de Entidades
* **ORDER_TABLE (Pedido)**: Cabecera del pedido de compra.
* **LINE_ITEM (Detalle del Pedido)**: Desglose por asiento comprado y precio de lote aplicado.
* **PROMOTION (Promoción / Código de Descuento)**: Campañas de descuentos aplicables a eventos.

---

## 5. Pagos (Payments)
* **Contexto Delimitado**: Payments
* **Propietario**: Payments Service
* **Propósito**: Procesamiento de pagos con pasarelas externas y liquidación diferida de fondos a organizadores.

```mermaid
erDiagram
    PAYMENT {
        uuid paymentId PK
        uuid orderId "ref: Orders.orderId"
        uuid buyerId "ref: Identity.userId"
        decimal amount
        decimal serviceFee
        string currency
        string method "CARD | TRANSFER"
        string status "INITIATED | AUTHORIZED | FAILED"
        string gatewayReference "ID de transacción externa"
        string idempotencyKey UK
        datetime createdAt
    }

    PAYOUT {
        uuid payoutId PK
        uuid organizerId "ref: Identity.userId"
        uuid eventId "ref: EventManagement.eventId"
        uuid dateId "ref: EventManagement.dateId"
        decimal grossAmount
        decimal serviceFeeTotal
        decimal netAmount
        string status "PENDING | PROCESSED | FAILED"
        integer retryCount "máximo 1 reintento automático"
        datetime triggeredAt
        datetime processedAt "null hasta que se procesa"
    }
```

### Descripción de Entidades
* **PAYMENT (Pago)**: Registro de los intentos de cobro a compradores. Exige `idempotencyKey` para evitar cargos dobles.
* **PAYOUT (Liquidación / Pago a Organizador)**: Desembolso al organizador. Se calcula asíncronamente una vez que finaliza la fecha del evento (`ADR-009`).

---

## 6. Emisión de Tickets (Ticket Issuance)
* **Contexto Delimitado**: Ticket Issuance
* **Propietario**: Ticket Issuance Service
* **Propósito**: Generación del boleto digital final, incluyendo códigos QR dinámicos con tiempo de vida (TTL) rotativo (`ADR-006`).

```mermaid
erDiagram
    TICKET {
        uuid ticketId PK
        uuid orderId "ref: Orders.orderId"
        uuid buyerId "ref: Identity.userId"
        uuid eventId "ref: EventManagement.eventId"
        uuid dateId "ref: EventManagement.dateId"
        uuid seatId "ref: SeatingInventory.seatId, null para GA"
        string type "MAPPED | GENERAL_ADMISSION"
        string holderName
        string qrCode "dinámico, expira cada 2 min"
        datetime qrExpiresAt "control de TTL"
        string accessPolicy
        string status "ISSUED | CANCELED | INVALIDATED | USED"
        datetime deliveredAt "null hasta entrega exitosa"
        datetime issuedAt
    }

    TICKET_DELIVERY {
        uuid deliveryId PK
        uuid ticketId FK
        string channel "EMAIL | PDF | QR | WALLET | DOWNLOAD"
        string status "PENDING | DELIVERED | FAILED"
        datetime deliveredAt
    }

    TICKET ||--o{ TICKET_DELIVERY : "entregado vía"
```

### Descripción de Entidades
* **TICKET (Boleto/Ticket)**: Activo digital que acredita el derecho de entrada. El código QR cambia periódicamente para evitar reventa o duplicación ilegal.
* **TICKET_DELIVERY (Entrega del Boleto)**: Seguimiento multicanal del proceso de envío del boleto.

---

## 7. Control de Acceso (Access Control)
* **Contexto Delimitado**: Access Control
* **Propietario**: Access Control Service
* **Propósito**: Registro en tiempo real de los escaneos de entradas en puertas de acceso. Soporta validación offline y conciliación asíncrona mediante el principio de "El Primer Escaneo Gana" (*First-Scan-Wins*, `ADR-007`).

```mermaid
erDiagram
    VALIDATION_RECORD {
        uuid validationId PK
        uuid ticketId "ref: TicketIssuance.ticketId (unidireccional)"
        string validatorDeviceId
        uuid eventId "ref: EventManagement.eventId"
        uuid dateId "ref: EventManagement.dateId"
        datetime attemptedAt
        string result "SUCCEEDED | FAILED"
        string failureReason "ALREADY_USED | WRONG_EVENT | EXPIRED | INVALIDATED | null"
        boolean isOffline "indica si el dispositivo estaba offline al escanear"
        datetime syncedAt "null hasta ser reconciliado"
        boolean conflictDetected "true si se gatilló first-scan-wins"
    }
```

### Descripción de Entidades
* **VALIDATION_RECORD (Registro de Validación)**: Tabla inmutable (append-only) que registra cada intento de lectura del QR en los accesos físicos.
  * `isOffline`: Soporta el almacenamiento local temporal en dispositivos portátiles.
  * `conflictDetected`: Activa flujos de auditoría si se detecta duplicidad del mismo ticket tras una sincronización posterior.

---

## 8. Notificaciones (Notifications)
* **Contexto Delimitado**: Notifications
* **Propietario**: Notifications Service
* **Propósito**: Envío de correos electrónicos, SMS o alertas de WhatsApp basadas en eventos de dominio del sistema.

```mermaid
erDiagram
    NOTIFICATION {
        uuid notificationId PK
        uuid recipientId "ref: Identity.userId"
        string channel "EMAIL | SMS | WHATSAPP"
        string templateId "referencia a plantilla de mensaje"
        json payload "datos dinámicos del mensaje"
        string status "PENDING | DISPATCHED | DELIVERED | FAILED"
        integer retryCount "soporta reenvíos manuales desde administración"
        string triggeredBy "evento de dominio origen"
        datetime createdAt
    }
```

---

## 9. Reportes (Reporting)
* **Contexto Delimitado**: Reporting
* **Propietario**: Reporting Service
* **Propósito**: Generación de reportes analíticos y financieros mediante proyecciones de lectura asíncronas basadas en eventos. **Nunca realiza consultas directas a otras bases de datos.**

```mermaid
erDiagram
    SALES_REPORT {
        uuid reportId PK
        uuid organizerId "ref: Identity.userId"
        uuid eventId "ref: EventManagement.eventId"
        uuid dateId "ref: EventManagement.dateId"
        integer totalTicketsSold
        decimal totalRevenue
        decimal totalServiceFees
        decimal totalPayouts
        datetime generatedAt
    }

    COMMISSION_REPORT {
        uuid reportId PK
        uuid organizerId "ref: Identity.userId"
        datetime periodStart
        datetime periodEnd
        decimal totalServiceFees
        datetime generatedAt
    }

    ACCESS_REPORT {
        uuid reportId PK
        uuid eventId "ref: EventManagement.eventId"
        uuid dateId "ref: EventManagement.dateId"
        integer totalValidations
        integer succeeded
        integer failed
        integer offlineScans
        integer conflictsDetected
        datetime generatedAt
    }
```

### Descripción de Entidades
* **SALES_REPORT (Reporte de Ventas)**: Agrega métricas financieras para los organizadores de un evento en una fecha específica.
* **COMMISSION_REPORT (Reporte de Comisiones)**: Resume las tarifas de servicio de la plataforma recaudadas durante un período de tiempo para la administración.
* **ACCESS_REPORT (Reporte de Accesos)**: Consolida el flujo de ingresos al recinto, porcentaje de fallas de QR, lecturas offline y conflictos detectados.
