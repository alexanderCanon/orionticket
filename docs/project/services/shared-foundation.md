# Base Compartida y Estándares Transversales

Este documento contiene la información técnica transversal que TODO el equipo debe conocer y aplicar, independientemente del microservicio que estén desarrollando.

---

## 1. Diseño y Arquitectura Core
*   **Lenguaje Ubicuo:** [Glosario de Términos](../phases/phase-1/ubiquitous-language-glossary.md) (Usa estos nombres estrictamente en código y base de datos).
*   **Reglas de Negocio Generales:** [Reglas de Negocio](../phases/phase-2/business-rules.md) (Sección Cross-cutting).
*   **Requerimientos No Funcionales:** [NFRs](../phases/phase-3/non-functional-requirements.md) (Rendimiento, Seguridad, SLAs).
*   **Diagrama de Despliegue:** [Topología del Sistema](../phases/phase-3/deployment-diagram.md).

## 2. Estándares de Desarrollo
Estos documentos son tu guía de calidad. **No inventes soluciones, sigue los estándares:**
*   **Guía de Producción (Microservicios):** [Guía de Microservicios](../standards/spring-microservices-production-guide.md).
*   **Base Compartida:** [Estándar de Base Compartida](../standards/spring-shared-foundation-standard.md).

## 3. Estructura de Proyecto (Arquitectura Hexagonal)
Para garantizar la independencia del dominio, todo microservicio debe seguir esta estructura de paquetes obligatoriamente:
```text
<feature>/
  domain/         (Modelos de negocio, Puertos de salida, Lógica pura)
  application/    (Casos de uso, Servicios de aplicación, Puertos de entrada)
  infrastructure/ (Controladores REST, Adaptores Persistence/Messaging, Configuración)
```
- **Regla de Oro:** El `domain` no debe depender de Spring, JPA ni de ninguna infraestructura externa.

## 4. Patrones de Diseño Transversales
*   **Idempotencia:** Todos los servicios deben validar la [Estrategia de Idempotencia (ADR-008)](../phases/phase-3/adrs/ADR-008-idempotency-key.md).
*   **Auditoría:** Todas las acciones sensibles deben registrarse en el [Log de Auditoría (ADR-012)](../phases/phase-3/adrs/ADR-012-auditlog-cross-cutting.md).
*   **Mensajería:** El [Broker de Mensajes (RabbitMQ)](../phases/phase-3/adrs/ADR-014-message-broker.md) es la columna vertebral de la comunicación asíncrona.
*   **API Gateway:** Toda comunicación externa pasa por el [Spring Cloud Gateway (ADR-015)](../phases/phase-3/adrs/ADR-015-api-gateway.md).

## 5. Infraestructura de Desarrollo
*   **Entorno Local:** [Docker Compose (ADR-017)](../phases/phase-3/adrs/ADR-017-container-orchestration.md).
*   **Base de Datos:** PostgreSQL (una instancia por servicio, sin llaves foráneas externas).
*   **Migraciones:** Flyway es obligatorio para cualquier cambio de esquema.
