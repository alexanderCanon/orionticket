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

## Prueba inicial: RabbitMQ

Antes de levantar microservicios contra la base externa, validar primero que el
compose renderiza correctamente y que RabbitMQ queda vivo.

Requisitos previos:

- `.env.local` debe existir o se debe usar `ORION_ENV_FILE` apuntando a otro
  archivo valido.
- `IP_NUBE` debe estar definido para que Compose pueda interpolar
  `compose.local.yml`, aunque RabbitMQ no use la base de datos.
- `RABBITMQ_USER` y `RABBITMQ_PASSWORD` deben mantenerse en `guest`/`guest`
  mientras el contenedor no defina credenciales propias con
  `RABBITMQ_DEFAULT_USER` y `RABBITMQ_DEFAULT_PASS`.

Comandos:

```sh
docker compose -f compose.local.yml --env-file .env.local config
docker compose -f compose.local.yml --env-file .env.local up -d rabbitmq
docker compose -f compose.local.yml ps rabbitmq
```

RabbitMQ debe quedar `running` y saludable antes de iniciar cualquier
microservicio. Luego se recomienda levantar servicios uno por uno para aislar
fallos de conexion a PostgreSQL, Flyway o mensajeria.

Orden operativo sugerido:

```text
rabbitmq
identity-service
event-management-service
seating-inventory-service
orders-service
payments-service
ticket-issuance-service
notifications-service
```

## Estrategia pendiente ante caidas de RabbitMQ

La tolerancia a caidas del broker no debe resolverse con publicacion directa
desde la logica de negocio. La estrategia recomendada para servicios que emiten
eventos criticos es el patron transactional outbox:

- Persistir el cambio de negocio y el evento pendiente en la base de datos del
  mismo servicio, dentro de la misma transaccion.
- Publicar eventos desde un worker o scheduler separado.
- Reintentar publicaciones fallidas cuando RabbitMQ vuelva a estar disponible.
- Hacer consumidores idempotentes para tolerar reintentos y entregas duplicadas.
- Definir retencion, limpieza y observabilidad de la tabla outbox.

Esta estrategia queda documentada como decision operativa pendiente. No debe
bloquear la primera prueba del laboratorio, cuyo alcance es validar RabbitMQ,
conexion a PostgreSQL via HAProxy, creacion de schemas por Flyway y arranque
basico de microservicios.

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
