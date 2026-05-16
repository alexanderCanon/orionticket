# ADR-018: JWT Validation with JWKS

| Field | Value |
|---|---|
| **Date** | 2026-05-15 |
| **Status** | PROPOSED |

## Context

OrionTicket requires protected HTTP APIs for Buyer, Organizer, Super Admin, Validator, and Support actors. ADR-015 already selects Spring Cloud Gateway and requires JWT validation at the gateway level without per-request calls to Identity.

The current implementation is not yet aligned with the final security model. Identity issues JWTs, but token validation, roles, authority mapping, and service-to-service enforcement are still incomplete. Access Control is already configured as an OAuth2 Resource Server expecting a JWKS URI, while Identity does not yet expose a JWKS endpoint.

## Decision

Use **asymmetric JWT signing in Identity** and expose a **JWKS endpoint** for token verification.

Identity is the issuer of access tokens. The API Gateway validates access tokens using Identity's JWKS endpoint and forwards authenticated user context to downstream services. Microservices that expose protected endpoints must also be able to validate JWTs as OAuth2 Resource Servers when called directly in local development or internal testing.

## Target Contract

- Identity signs access tokens with a private key.
- Identity exposes the public key set at `/.well-known/jwks.json`.
- Tokens include stable claims required by the platform:
  - `sub` as the authenticated user ID.
  - `email` when available.
  - `role` or authorities compatible with the Actor-Role Map.
  - `organizerId` when the user belongs to an organizer context.
  - `iss`, `iat`, `exp`, and `kid`.
- Spring Cloud Gateway validates incoming tokens through `IDENTITY_JWKS_URI`.
- Services read authenticated context from Spring Security and enforce ownership and role rules in the application layer.
- Public endpoints remain explicitly whitelisted, for example login, registration, OpenAPI, actuator health, and callbacks where documented.

## Required Configuration

| Variable | Owner | Purpose |
|---|---|---|
| `JWT_ISSUER` | Identity, Gateway, services | Expected token issuer. |
| `JWT_KEY_ID` | Identity | Key identifier exposed in JWT header and JWKS. |
| `JWT_PRIVATE_KEY` | Identity | Private key used to sign tokens. |
| `JWT_PUBLIC_KEY` | Identity | Public key exposed through JWKS. |
| `JWT_EXPIRATION` | Identity | Access token lifetime. |
| `IDENTITY_JWKS_URI` | Gateway, services | URI used to validate JWT signatures. |

## Consequences

- The shared-secret JWT model is replaced by a safer public/private key model.
- Gateway and services can validate tokens without calling Identity on every request.
- Local development requires a stable local key pair or mounted environment variables.
- Key rotation becomes possible through `kid`, but the rotation process is deferred until after the MVP security baseline is implemented.
- Role and permission enforcement must be implemented consistently across protected controllers; hardcoded user IDs in controllers must be removed.

## Non-Goals for MVP

- Full OAuth2 authorization server implementation.
- Refresh token rotation.
- Session revocation lists.
- Multi-tenant key rotation.
- External identity providers such as Auth0, Cognito, or Keycloak.

## Implementation Notes

1. Update Identity to sign JWTs with an asymmetric key pair.
2. Add the JWKS endpoint in Identity.
3. Update Gateway to validate tokens using `IDENTITY_JWKS_URI`.
4. Update protected microservices to use Spring Security OAuth2 Resource Server where direct service testing requires token validation.
5. Map JWT roles/authorities to the documented Actor-Role Map.
6. Replace temporary controller placeholders with `SecurityContext`-derived user identity.

## Related Documents

- [ADR-015: API Gateway Selection](ADR-015-api-gateway.md)
- [Actor Role Map](../../phase-2/actor-role-map.md)
- [System Flow Diagrams](../../phase-2/system-flow-diagrams.md)
- [Identity Service Manual](../../../project/services/01-identity.md)
