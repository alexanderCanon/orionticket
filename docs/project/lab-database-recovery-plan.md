# Plan de recuperacion de laboratorio con base limpia

## Objetivo

Crear una base de datos limpia para el laboratorio `compose.local.yml` y corregir
la configuracion de schemas antes de volver a levantar microservicios. La base
anterior no debe usarse para validar el arranque porque algunas tablas quedaron
en schemas incorrectos durante la prueba inicial.

## 1. Detener servicios levantados

Detener solo los microservicios que hayan arrancado. RabbitMQ puede quedarse
vivo.

```sh
docker compose -f compose.local.yml --env-file .env.local stop identity-service event-management-service
```

## 2. Crear una base nueva

Crear una base limpia en PostgreSQL/Patroni, por ejemplo:

```sql
CREATE DATABASE orionticketdb_lab2;
```

Si se usa el usuario `postgres`, no se requieren grants adicionales. Si se usa
otro usuario, debe tener permisos para:

```sql
CREATE SCHEMA;
CREATE TABLE;
CREATE INDEX;
ALTER TABLE;
CREATE SEQUENCE;
```

## 3. Configurar schemas sin startup parameters

No usar `currentSchema` en la URL JDBC porque el endpoint por HAProxy/pooler lo
rechaza como startup parameter. No usar `connection-init-sql` como solucion final
porque puede dejar Flyway y las conexiones en schemas inconsistentes.

Patron recomendado por servicio:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5000}/${GLOBAL_DB_NAME:orionticketdb_lab2}
  jpa:
    properties:
      hibernate:
        default_schema: ${DB_SCHEMA:<schema_del_servicio>}
  flyway:
    enabled: true
    schemas: ${DB_SCHEMA:<schema_del_servicio>}
    default-schema: ${DB_SCHEMA:<schema_del_servicio>}
    create-schemas: true
```

Ejemplo para Identity:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5000}/${GLOBAL_DB_NAME:orionticketdb_lab2}
    username: ${GLOBAL_DB_USER:app_user}
    password: ${GLOBAL_DB_PASSWORD:AppSecret789}
    driver-class-name: org.postgresql.Driver
    hikari:
      minimum-idle: 10

  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        default_schema: ${DB_SCHEMA:identity}

  flyway:
    enabled: true
    schemas: ${DB_SCHEMA:identity}
    default-schema: ${DB_SCHEMA:identity}
    create-schemas: true
    baseline-on-migrate: true
```

Schemas esperados:

```text
identity
event_management
seating_inventory
orders
payments
ticket_issuance
notifications
access_control
reporting
```

## 4. Actualizar `.env.local`

Apuntar el laboratorio a la base nueva:

```env
GLOBAL_DB_NAME=orionticketdb_lab2
```

No definir `DB_SCHEMA` globalmente en `.env.local`. Cada servicio debe usar su
propio default documentado en `application.yml` o `application.yaml`.

## 5. Validar Compose sin levantar servicios

```sh
docker compose -f compose.local.yml --env-file .env.local config
```

## 6. Levantar Identity primero

```sh
docker compose -f compose.local.yml --env-file .env.local up -d --build identity-service
docker compose -f compose.local.yml --env-file .env.local logs -f identity-service
```

Evidencia esperada en logs:

```text
Creating schema "identity"
Successfully applied ... migrations to schema "identity"
Started IdentityServiceApplication
```

Verificar en PostgreSQL:

```sql
SELECT table_schema, table_name
FROM information_schema.tables
WHERE table_schema IN ('public', 'identity')
ORDER BY table_schema, table_name;
```

Resultado esperado:

```text
identity.flyway_schema_history
identity.permissions
identity.roles
identity.users
```

`public` puede contener extensiones como `pg_stat_statements`; no debe contener
tablas de negocio de OrionTicket.

## 7. Levantar Event Management

```sh
docker compose -f compose.local.yml --env-file .env.local up -d --build event-management-service
docker compose -f compose.local.yml --env-file .env.local logs -f event-management-service
```

Verificar en PostgreSQL:

```sql
SELECT table_schema, table_name
FROM information_schema.tables
WHERE table_schema IN ('event_management', 'public')
ORDER BY table_schema, table_name;
```

Resultado esperado:

```text
event_management.event_dates
event_management.events
event_management.flyway_schema_history
event_management.venues
```

## 8. Continuar servicio por servicio

Orden sugerido:

```text
seating-inventory-service
orders-service
payments-service
ticket-issuance-service
notifications-service
```

No levantar todos juntos hasta confirmar que cada servicio crea y usa su schema
correcto.

## 9. Reglas de seguridad operativa

- No usar `?currentSchema=...` en URLs JDBC detrás de HAProxy/pooler.
- No usar `connection-init-sql: SET search_path TO ...` como solucion final.
- No definir `DB_SCHEMA` globalmente en `.env.local`.
- No validar contra una base que ya tenga tablas de negocio en `public` o tablas
  cruzadas en schemas de otro servicio.
