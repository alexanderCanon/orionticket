# Resultados de Pruebas AutomÃ¡ticas (E2E y Salud)
Fecha de ejecuciÃ³n: 05/19/2026 20:33:30

## 1. VerificaciÃ³n de Salud de Microservicios (Actuator)

| Microservicio | Puerto | Estado HTTP | Resultado |
|---|---|---|---|
| ticket-issuance-service | 8086 | ERROR | âŒ FALLO |
| identity-service | 8081 | 200 OK | âœ… UP |
| reporting-service | 8089 | 200 OK | âœ… UP |
| seating-inventory-service | 8083 | 200 OK | âœ… UP |
| notifications-service | 8088 | 503 | âŒ FALLO |
| gateway-service | 8080 | 200 OK | âœ… UP |
| access-control-service | 8087 | 200 OK | âœ… UP |
| payments-service | 8085 | 200 OK | âœ… UP |
| event-management-service | 8082 | 200 OK | âœ… UP |
| orders-service | 8084 | 200 OK | âœ… UP |

## 2. Flujo CrÃ­tico a travÃ©s del API Gateway (Puerto 8080)

### Registro de Usuario (Identity-Service)

Status: NotFound âŒ

### ComprobaciÃ³n de Swagger UI en Gateway

Status: NotFound âŒ