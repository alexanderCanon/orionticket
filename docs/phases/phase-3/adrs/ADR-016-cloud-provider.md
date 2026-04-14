# ADR-016: Cloud Provider Selection

| Field | Value |
|---|---|
| **Date** | 2026-04-13 |
| **Status** | ACCEPTED |

## Context

OrionTicket requires cloud infrastructure for hosting 9+ microservices, databases, message broker, API gateway, and observability stack. The initial market is Guatemala, so latency to Central American users matters. Budget is $50,000 USD total (including development), which constrains infrastructure spend.

## Decision

**VPS (self-hosted).**

## Justification

- Lowest cost option for a $50K total budget — avoids managed-service premiums from AWS/GCP/Azure.
- Full infrastructure control — no vendor lock-in on managed services.
- Dedicated DevOps role (1 engineer) absorbs the operational burden of self-hosting.
- VPS providers (e.g., Hetzner, DigitalOcean, Contabo) offer US-based or Latin American data centers with acceptable latency to Guatemala.
- Docker Compose deployment (ADR-017) runs directly on VPS without requiring managed container orchestration.

## Options Considered

## Options to Evaluate

| Option | Closest Region | Managed K8s | Managed DB | Startup Credits | Guatemala Latency |
|---|---|---|---|---|---|
| **AWS** | us-east-1 (Virginia) | EKS | RDS, DynamoDB | AWS Activate | ~30–50 ms |
| **GCP** | us-central1 (Iowa) | GKE | Cloud SQL, Firestore | Google for Startups | ~40–60 ms |
| **Azure** | South Central US (Texas) | AKS | Azure SQL, Cosmos DB | Microsoft for Startups | ~35–55 ms |
| **DigitalOcean** | NYC / SFO | DOKS | Managed PostgreSQL | Hatch (startup) | ~30–50 ms |

## Evaluation Criteria

1. Latency from Guatemala City to nearest region (target: ≤ 50 ms for QR validation path edge).
2. Managed services available for databases, message broker, and container orchestration.
3. Total infrastructure cost estimate within budget.
4. Payment gateway SDK compatibility (Guatemala-compatible card and transfer processors).
5. Team familiarity.
6. Data protection compliance (Guatemalan law — discovery.md Block 7.4).

## Source

- discovery.md — Block 2.1 (Guatemala only)
- discovery.md — Block 8.4 (latency targets)
- discovery.md — Block 10.5 (budget)
