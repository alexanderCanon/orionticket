# Microservicio: Identity (Identidad)

Responsable de la autenticación, autorización y gestión de usuarios/roles. Es la base de seguridad de todo el sistema.

---

## 1. Responsabilidades
- Registro y Login de usuarios.
- Generación y validación de tokens JWT.
- Gestión de roles y permisos (Actor-Role Map).
- Gestión de staff de organizaciones (Organizadores).

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
