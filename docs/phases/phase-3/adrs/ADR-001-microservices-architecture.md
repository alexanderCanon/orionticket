# ADR-001: Microservices as Architectural Style

| Field | Value |
|---|---|
| **Date** | 2026-04-13 |
| **Status** | ACCEPTED |

## Context

OrionTicket is a white-label, multi-tenant ticket sales platform targeting massive events with up to 30,000 concurrent users. The client explicitly expects independently deployed microservices from day 1. However, the budget is $50,000 USD with a 3-month deadline and only 2 backend developers — creating significant tension between the architectural ambition and the delivery constraints.

## Options Considered

1. **Monolith-first** — Single deployable unit, modular internal structure, extract services later.
2. **Microservices from day 1** — Independent services per bounded context, separate databases, async communication.
3. **Modular monolith with service extraction plan** — Internal module boundaries matching bounded contexts, shared runtime, planned extraction after v1.

## Decision

**Microservices from day 1.** This is a client constraint, not a team recommendation. The architectural style is accepted as a hard requirement.

## Justification

- The client's stated expectation is independently deployed microservices for scalability and resilience (discovery.md Block 10.1, 10.2).
- The team acknowledges the risk and mitigates it by: defining a strict MVP cut (4–5 core services in v1, others stubbed), phased roadmap across 8 sprints, and prioritizing core business services first (discovery.md Block 10.8, Block 12.10).
- The priority is "strong technical foundation for the long term over speed-to-market" (discovery.md Block 10.7).

## Consequences

- **Positive:** Independent scaling of high-contention services (Seating/Inventory, Orders). Fault isolation. Clear ownership boundaries.
- **Negative:** Operational complexity for a small team. Distributed tracing and observability are mandatory from day 1 (not optional). Higher infrastructure cost. Risk of under-delivery within budget/timeline.
- **Mitigation:** Strict MVP scope. Observability infrastructure treated as non-negotiable. Runbooks and automated alerting defined pre-launch.

## Source

- discovery.md — Block 10.1, 10.2, 10.7, 10.8
- discovery.md — Block 12.6 (risk table)
