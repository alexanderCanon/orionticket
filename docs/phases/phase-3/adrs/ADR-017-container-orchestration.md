# ADR-017: Container Orchestration Selection

| Field | Value |
|---|---|
| **Date** | 2026-04-13 |
| **Status** | ACCEPTED |

## Context

The platform consists of 9+ independently deployed microservices, each with its own database. Container orchestration is needed for deployment, scaling, health checks, rolling updates, and service discovery. The team includes 1 DevOps member, which constrains the operational complexity budget.

## Decision

**Docker Compose.**

## Justification

- Sufficient for MVP scope — 9 services on a single VPS (ADR-016) do not require full orchestration.
- Minimal learning curve — the entire team is already familiar with Docker Compose.
- Straightforward local development parity — same `docker-compose.yml` runs locally and in production.
- Can evolve to Docker Swarm or Kubernetes post-MVP when scaling demands require multi-node orchestration.
- Health checks, restart policies, and resource limits are supported natively in Compose.
- No additional infrastructure or managed service cost.

## Options Considered

## Options to Evaluate

| Option | Auto-scaling | Service Discovery | Zero-Downtime Deploy | Learning Curve | Cost |
|---|---|---|---|---|---|
| **Kubernetes (EKS/GKE/AKS)** | HPA + VPA | Built-in (DNS) | Rolling updates | High | Managed: $70–150/mo per cluster |
| **Docker Swarm** | Replicas-based | Built-in (DNS) | Rolling updates | Low | Self-hosted only |
| **ECS Fargate (AWS)** | Target tracking | Service discovery (Cloud Map) | Rolling + blue/green | Medium | Pay-per-task |
| **Cloud Run (GCP)** | Request-based | Built-in | Revisions | Low | Pay-per-request |
| **Nomad** | Job scaling | Consul integration | Canary + rolling | Medium | Free OSS |

## Evaluation Criteria

1. Auto-scaling per service — Seating/Inventory and Orders must scale independently during 30K concurrent user peak (discovery.md Block 8.2).
2. Health checks and automatic restart of failed containers (99.9% SLA — discovery.md Block 8.3).
3. Zero-downtime deployments (rolling or blue/green).
4. Operational complexity manageable by 1 DevOps engineer.
5. Compatibility with chosen cloud provider (ADR-016).
6. Cost within infrastructure budget.

## Source

- discovery.md — Block 8.2 (peak load), Block 8.3 (SLA)
- discovery.md — Block 10.2 (independently deployed)
- discovery.md — Block 10.3 (team: 1 DevOps)
