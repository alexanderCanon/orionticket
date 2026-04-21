# Microservicio: Identity (Identidad)

Responsable de la autenticación, autorización y gestión de usuarios/roles. Es la base de seguridad de todo el sistema.

---

## 1. Responsabilidades
- Registro y Login de usuarios.
- Generación y validación de tokens JWT.
- Gestión de roles y permisos (Actor-Role Map).
- Gestión de staff de organizaciones (Organizadores).

## 2. Documentación Técnica Específica
- **Casos de Uso:** [UC-ID-01, UC-ID-02, UC-ID-03](../docs/phases/phase-2/use-case-catalog.md).
- **Agregado Maestro:** [User / Role](../docs/phases/phase-1/aggregate-definitions.md) (Ver sección Identity).
- **Contrato API:** [Identity Service Endpoints](../docs/phases/phase-3/service-contracts.md) (Sección Identity).
- **Diagrama ER:** [Modelo de Datos de Identity](../docs/phases/phase-3/er-diagrams/identity.md).

## 3. Eventos de Dominio
### Genera (Produce):
- `OrganizerRegistered`
- `OrganizerApproved`
- `UserSuspended`

---

## 4. Estándares Aplicables
- [Fase 4: Definition of Done](../docs/phases/phase-4/definition-of-done.md).
- [Día 10: Auth JWT Blocker](../project/implementation-guide.md).
