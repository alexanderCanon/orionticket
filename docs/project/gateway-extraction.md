# Extraccion del API Gateway

## Objetivo

Separar `gateway-service` del monorepo para operarlo como `orion-api-gateway`
en una VPS independiente. Traefik quedara delante para HTTPS y el gateway
hablara con los microservicios por IPs directas de Tailscale.

## Alcance actual

Incluido:

- Spring Cloud Gateway WebFlux.
- Actuator.
- Dockerfile.
- Compose con Traefik.
- Rutas a servicios esenciales y `notifications-service`.

Excluido por ahora:

- Eureka.
- Spring Cloud Config Server.
- RabbitMQ en el gateway.
- Redis.
- Observabilidad avanzada / telemetry.
- Kubernetes.
- `access-control-service`.
- `reporting-service`.

## Servicios publicados detras del gateway

| Servicio | Puerto | Estado |
|---|---:|---|
| `identity-service` | 8081 | Incluido |
| `event-management-service` | 8082 | Incluido |
| `seating-inventory-service` | 8083 | Incluido |
| `orders-service` | 8084 | Incluido |
| `payments-service` | 8085 | Incluido |
| `ticket-issuance-service` | 8086 | Incluido |
| `notifications-service` | 8088 | Incluido |
| `access-control-service` | 8087 | Fuera |
| `reporting-service` | 8089 | Fuera |

## Archivos operativos

- `compose.local.yml`: laboratorio backend sin gateway, sin Access Control y sin Reporting.
- El codigo de `orion-api-gateway` vive en su repositorio independiente.

## Comandos de laboratorio

Desde el monorepo:

```sh
cp .env.example .env
docker compose -f compose.yml --env-file .env config
docker compose -f compose.yml --env-file .env up -d --build
```

Para validar sin crear `.env`, usar:

```sh
ORION_ENV_FILE=.env.example docker compose -f compose.yml --env-file .env.example config
```

Para levantar el laboratorio de 7 servicios contra HAProxy y el gateway externo:

```sh
cp .env.local.example .env.local
docker compose -f compose.local.yml --env-file .env.local config
docker compose -f compose.local.yml --env-file .env.local up -d --build
```

Para validar sin crear `.env`, usar:

```sh
ORION_ENV_FILE=.env.local.example docker compose -f compose.local.yml --env-file .env.local.example config
```

## Variables del gateway

Reemplazar los valores de ejemplo por IPs reales de Tailscale:

```text
IDENTITY_SERVICE_URL=http://100.x.y.z:8081
EVENT_MANAGEMENT_SERVICE_URL=http://100.x.y.z:8082
SEATING_INVENTORY_SERVICE_URL=http://100.x.y.z:8083
ORDERS_SERVICE_URL=http://100.x.y.z:8084
PAYMENTS_SERVICE_URL=http://100.x.y.z:8085
TICKET_ISSUANCE_SERVICE_URL=http://100.x.y.z:8086
NOTIFICATIONS_SERVICE_URL=http://100.x.y.z:8088
```

## Notas

- El gateway no debe tener `depends_on` hacia microservicios remotos.
- Si un microservicio esta apagado, solo deben fallar sus rutas.
- Los puertos backend deben protegerse con firewall para que sean accesibles
  por Tailscale y no por Internet publico.
