# Ticket Issuance Service

Microservicio responsable de la emision de boletos, QR dinamico y estado posterior a la emision.

## Alcance inicial

- Consultar ticket por `ticketId`.
- Listar tickets por comprador.
- Preparar cancelacion y reenvio manual.
- Preparar consumo de `PaymentAuthorized`.
- Preparar publicacion de `TicketIssued`, `TicketDelivered`, `TicketCanceled` y `TicketInvalidated`.

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
