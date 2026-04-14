# ADR-015: API Gateway Selection

| Field | Value |
|---|---|
| **Date** | 2026-04-13 |
| **Status** | ACCEPTED |

## Context

All client-facing frontends (Buyer Portal, Organizer Panel, Super Admin Panel, Validator App) communicate with backend services through a single entry point. An API gateway is needed for routing, rate limiting, authentication forwarding, and anti-bot protection. The gateway must support the anti-fraud perimeter required by the platform (rate limiting per user/IP, virtual queue for high-demand events).

## Decision

**Spring Cloud Gateway.**

## Justification

- Java-native — consistent with the backend stack. No additional runtime or language to operate.
- Integrates directly with Spring Security for JWT validation at the gateway level, eliminating per-request calls to the Identity service.
- Built-in rate limiting filters (RequestRateLimiter with Redis backing).
- WebSocket support for Validator App real-time communication.
- No additional infrastructure cost — runs as another Spring Boot service on the same VPS (ADR-016).
- Team already proficient in Spring ecosystem — minimal learning curve.

## Options Considered

## Options to Evaluate

| Option | Rate Limiting | Auth | WebSocket | Managed cost | Complexity |
|---|---|---|---|---|---|
| **Kong** | Plugin-based, flexible | JWT/OAuth plugin | Supported | Free OSS / Enterprise paid | Medium |
| **AWS API Gateway** | Built-in throttling | Cognito / Lambda authorizer | WebSocket API available | Pay-per-request | Low |
| **Traefik** | Middleware-based | ForwardAuth | Supported | Free OSS | Low–Medium |
| **Envoy** | Rate limit service | ext_authz filter | Supported | Free OSS | High |

## Evaluation Criteria

1. Rate limiting per user/IP at gateway level (required for anti-fraud — discovery.md Block 7.2).
2. JWT/token validation without per-request call to Identity service.
3. WebSocket support for Validator App real-time communication.
4. Virtual queue integration capability (holding requests during presale spikes).
5. Team operational expertise.
6. Cost within budget.
7. Compatibility with container orchestration choice (ADR-017).

## Source

- discovery.md — Block 7.2 (anti-fraud mechanisms)
- discovery.md — Block 8.2 (30K concurrent users)
- discovery.md — Block 11.9 (API versioning)
