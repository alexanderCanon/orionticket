# Notifications Service

Microservicio responsable de consumir eventos de dominio, crear registros de notificacion y despachar mensajes por canales externos.

## Alcance inicial

- Persistir notificaciones.
- Preparar consumo de eventos como `TicketIssued`, `ReservationExpired` y `PaymentFailed`.
- Preparar envio por email como canal minimo del MVP.
- Preparar publicacion de `NotificationDispatched`, `NotificationDelivered` y `NotificationFailed`.
- Preparar reintentos y DLQ.

## Stack

- Java 21
- Spring Boot 3
- PostgreSQL
- Flyway
- RabbitMQ
- OpenAPI
- Testcontainers

## Ejecutar pruebas

```powershell
mvn test
```
