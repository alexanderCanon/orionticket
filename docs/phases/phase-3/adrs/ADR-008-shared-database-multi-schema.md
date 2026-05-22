# ADR 008: Shared Database with Multi-Schema Isolation

## Status
Accepted

## Context
El diseño original propuesto en el [ADR-001](ADR-001-microservices-architecture.md) exigía "PostgreSQL, one database per service" con el propósito de garantizar un aislamiento estricto de datos, evitando escenarios de *shared-write*. Sin embargo, mantener 9 instancias separadas de PostgreSQL operando concurrentemente genera un elevado consumo de memoria y CPU, especialmente crítico durante el desarrollo local y en el despliegue de un MVP en un VPS limitado.

Para reducir significativamente el *footprint* de infraestructura sin violar la regla de oro de aislamiento de datos ("Each service owns its data exclusively"), se plantea modificar la infraestructura física de la base de datos manteniendo el aislamiento lógico.

## Decision
Se decide migrar la arquitectura de persistencia a una **única instancia compartida de PostgreSQL** llamada `orionticketdb`, en la cual cada microservicio dispondrá de un **esquema lógico independiente** (por ejemplo: `identity_schema`, `orders_schema`, etc.).

1.  **Aislamiento Lógico:** Cada microservicio será configurado explícitamente para conectarse a la base de datos compartida inyectando su respectivo `currentSchema` en la URL de JDBC.
2.  **Gestión de Esquemas:** Flyway se encargará de crear y gestionar automáticamente el esquema designado para cada servicio (`spring.flyway.schemas` y `spring.flyway.create-schemas=true`).
3.  **Prohibición Mantenida:** Sigue estando estrictamente prohibido realizar *joins* o consultas cruzadas entre esquemas. La comunicación entre dominios debe seguir siendo asíncrona (vía RabbitMQ) o síncrona vía HTTP a las APIs.

## Consequences

### Positivas
- Reducción drástica del consumo de memoria RAM y CPU en entornos de desarrollo y producción (de 9 contenedores Postgres a 1).
- Simplificación del archivo `docker-compose.yml`.
- Administración centralizada de respaldos y volúmenes persistentes.

### Negativas
- Posible *noisy neighbor* effect: si un microservicio satura el I/O de la base de datos, podría afectar el rendimiento de los otros microservicios que comparten el mismo clúster físico de PostgreSQL.
- Dificultad para escalar independientemente el almacenamiento de un único microservicio en el futuro sin reconfigurar la base de datos global.

## Notes
Esta decisión revoca parcialmente la cláusula "una instancia de base de datos por servicio" descrita en la documentación previa, priorizando la viabilidad del despliegue del MVP bajo el principio rector de aislamiento lógico estricto.
