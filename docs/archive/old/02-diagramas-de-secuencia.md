# Diagramas de Secuencia: OrionTicket

Este documento detalla los flujos de interacción entre los microservicios utilizando **Mermaid**. Estos diagramas son la base para la implementación de la consistencia eventual y el manejo de alta concurrencia.

---

## 1. Flujo Exitoso: Reserva y Pago (Happy Path)
Este flujo utiliza **Redis** para locks rápidos (concurrencia) y **Kafka** para la confirmación asíncrona.

```mermaid
sequenceDiagram
    participant U as Comprador
    participant AGW as API Gateway
    participant BS as Booking Service
    participant R as Redis (Locks)
    participant IS as Inventory Service
    participant PS as Payment Service
    participant K as Kafka (Events)
    participant NS as Notification Service

    U->>AGW: POST /bookings (EventoID, Cantidad/Asiento)
    AGW->>BS: Request Booking
    BS->>R: SETNX lock:asiento:ID (TTL 5m)
    alt Lock Adquirido
        BS->>IS: Validar Stock/Disponibilidad
        IS-->>BS: Stock OK
        BS->>BS: Crear Reserva (Status: PENDING)
        BS-->>U: 201 Created (BookingID, PaymentURL)
    else Asiento Ocupado / Sin Stock
        BS-->>U: 409 Conflict / 422 Out of Stock
    end

    U->>PS: Procesar Pago (Mock)
    PS->>PS: Validar Tarjeta
    PS->>K: Emitir Evento: PAYMENT_SUCCESS
    K->>BS: Consumer: PAYMENT_SUCCESS
    BS->>BS: Actualizar Reserva (Status: CONFIRMED)
    BS->>K: Emitir Evento: BOOKING_FINALIZED
    K->>NS: Consumer: BOOKING_FINALIZED
    NS->>U: Enviar Ticket (Email/Push)
```

---

## 2. Expiración de Reserva (TTL Cleanup)
Manejo de la liberación de inventario cuando el usuario no completa el pago en los 5 minutos establecidos.

```mermaid
sequenceDiagram
    participant W as Worker / Scheduler
    participant BS as Booking Service
    participant R as Redis
    participant K as Kafka
    participant IS as Inventory Service

    W->>BS: Check Expired Bookings (Cron every 1m)
    BS->>BS: Buscar Reservas PENDING > 5m
    loop Cada Reserva Expirada
        BS->>R: DEL lock:asiento:ID
        BS->>BS: Actualizar Status: EXPIRED
        BS->>K: Emitir Evento: INVENTORY_RELEASED
        K->>IS: Consumer: INVENTORY_RELEASED
        IS->>IS: Reponer Stock / Liberar Asiento
    end
```

---

## 3. Manejo de Inconsistencia (Falla Crítica Post-Cobro)
Este diagrama modela el requisito de "fila de espera manual" cuando el cobro es exitoso pero la confirmación del ticket falla.

```mermaid
sequenceDiagram
    participant PS as Payment Service
    participant K as Kafka
    participant BS as Booking Service
    participant DB as DB (Incidencias)
    participant Dashboard as Admin Dashboard
    participant Admin as Administrador

    PS->>K: Emitir Evento: PAYMENT_SUCCESS
    K->>BS: Consumer: PAYMENT_SUCCESS
    Note over BS: Error Crítico: DB Caída o Bug Inesperado
    BS->>BS: Retry Logic (3 veces)
    alt Reintentos Fallidos
        BS->>DB: Registrar Incidencia (PaymentID, BookingID, Error)
        DB->>Dashboard: Notificar Nueva Incidencia
        Dashboard->>Admin: Alerta Visual
        Admin->>Dashboard: Revisar Transacción
        Admin->>BS: Resolver Manualmente (Confirmar/Reembolsar)
    end
```
