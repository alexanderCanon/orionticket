# Microservicio: Identity (Identidad)

Responsable de la autenticación, autorización y gestión de usuarios/roles. Es la base de seguridad de todo el sistema.

> Nota operativa: el código de `identity-service` ya no vive en este
> monorepo. Para el laboratorio MVP fue movido al repositorio operativo del
> gateway, donde se despliega junto con `orion-api-gateway` y Traefik. Este
> documento se mantiene como manual técnico y contrato de arquitectura del
> servicio.

---

## 1. Responsabilidades
- Registro y Login de usuarios.
- Generación y validación de tokens JWT.
- Gestión de roles y permisos (Actor-Role Map).
- Gestión de staff de organizaciones (Organizadores).

## 1.1 Modelo de autenticación actual

Para el MVP, Identity funciona como un servicio de autenticación propio basado
en Spring Security:

- Valida credenciales con email y contraseña.
- Persiste contraseñas con hash BCrypt.
- Emite JWT firmados con llave asimétrica.
- Expone JWKS en `/.well-known/jwks.json`.
- Mantiene roles y permisos propios en base de datos.

Este modelo se mantiene por simplicidad operativa del MVP. No implementa aún un
OAuth2 Authorization Server completo, refresh tokens, sesiones, revocación
centralizada ni federación con proveedores externos.

La evolución esperada, posterior al MVP, es acercar Identity a un modelo formal
de OAuth2 Authorization Server o evaluar una solución dedicada como Keycloak,
Auth0, Cognito u otra alternativa equivalente. Esa evolución requiere una nueva
decisión arquitectónica antes de cambiar contratos de tokens, flujos de login o
responsabilidades del gateway.

## 2. Documentación Técnica Específica
- **Casos de Uso:** [UC-ID-01, UC-ID-02, UC-ID-03, US-009](../../phases/phase-2/use-case-catalog.md).
- **Agregado Maestro:** [User / Role](../../phases/phase-1/aggregate-definitions.md) (Ver sección Identity).
- **Contrato API:** [Identity Service Endpoints](../../phases/phase-3/service-contracts.md) (Sección Identity).
- **Endpoint Staff (US-009):** `POST /v1/organizers/{organizerId}/staff` - Creación de validadores y staff.
- **Diagrama ER:** [Modelo de Datos de Identity](../../phases/phase-3/er-diagrams/identity.md).
- **Decisión de Seguridad:** [ADR-018: JWT Validation with JWKS](../../phases/phase-3/adrs/ADR-018-jwt-jwks-security.md).

## 3. Eventos de Dominio
### Genera (Produce):
- `OrganizerRegistered`
- `OrganizerApproved`
- `UserSuspended`

---

## 4. Estándares Aplicables
- [Fase 4: Definition of Done](../../phases/phase-4/definition-of-done.md).
- [Día 10: Auth JWT Blocker](../implementation-guide.md).
