# Team Coordination Guide

> **Project:** OrionTicket  
> **Team size:** 4 developers  
> **Duration:** 45 days (4 sprints)  
> **Definition of Done:** [docs/phases/phase-4/definition-of-done.md](../docs/phases/phase-4/definition-of-done.md)  
> **Documentation index:** [INDEX.md](../INDEX.md)

---

## 1. Service Ownership

Each person owns their bounded contexts end-to-end: backend (Spring Boot), tests, database migrations, domain events, and their frontend slice (Angular).

| Person | Bounded Contexts | Frontend Slice | Sprint Focus |
|---|---|---|---|
| **Person 1** | Identity, Event Management | Organizer Panel (event CRUD, staff management), Super Admin Panel (user/role management, event approval) | Sprint 1 primary, supports all sprints (auth dependency) |
| **Person 2** | Seating / Inventory | Organizer Panel (seating map config, batch management), Buyer Portal (seat selection view) | Sprint 2 primary |
| **Person 3** | Orders, Payments | Buyer Portal (checkout flow, payment form), Organizer Panel (order/payout views) | Sprint 2 primary |
| **Person 4** | Ticket Issuance, Access Control, Notifications, Reporting | Buyer Portal (ticket list, QR display), Validator App (scan UI, sync), Organizer Panel (reports) | Sprint 3–4 primary |

### Dependency order

```
Person 1 (Identity, Event Mgt) → Person 2 (Seating/Inventory) → Person 3 (Orders, Payments) → Person 4 (Ticket Issuance, Access Control, Notifications, Reporting)
```

- Person 1 *must* deliver Identity auth endpoints and Event Management core by end of Sprint 1. Everything downstream depends on it.
- Person 2 *must* deliver Reservation endpoint by mid-Sprint 2. Orders cannot integrate without it.
- Person 3 *must* deliver PaymentAuthorized event by end of Sprint 2. Ticket Issuance cannot start without it.
- Person 4 consumes events — blocked until upstream producers are live or mocked.

### Infrastructure ownership

| Concern | Owner |
|---|---|
| `docker-compose.yml` (root) | Person 1 (setup week), then shared |
| Spring Cloud Gateway config | Person 1 |
| RabbitMQ exchanges and queues | Each person declares their own; Person 1 seeds the shared config |
| CI/CD pipeline (GitHub Actions) | Person 1 (setup week) |
| Database migrations per service | Each person for their own services |

---

## 2. Branching Strategy

### Branch naming

```
{type}/{US-XXX}-{short-description}

Examples:
  feature/US-004-create-event
  fix/US-013-reservation-race-condition
  chore/rabbitmq-dlq-config
```

**Types:** `feature`, `fix`, `chore`, `docs`

### Branch model

| Branch | Purpose | Who merges | Protected |
|---|---|---|---|
| `main` | Production-ready. Tagged releases only. | Sprint lead (rotates per sprint) | Yes — requires 2 approvals |
| `develop` | Integration branch. All feature PRs merge here. | PR author after 1 approval | Yes — requires 1 approval |
| `feature/*` | Individual work. One branch per user story. | PR author after review | No |
| `fix/*` | Bug fixes. | PR author after review | No |

### PR rules

1. **Every PR requires at least 1 approval** from a team member who does NOT own that service.
2. **PR description must include:** user story ID (US-XXX), what changed, how to test.
3. **CI must pass** before merge is allowed (compile, tests, Docker build).
4. **Squash merge** to `develop`. Keep history clean.
5. **No direct commits to `develop` or `main`.** Enforcement via GitHub branch protection rules.
6. **Maximum PR size: 400 lines of diff.** If larger, split into sub-tasks.

### Merge policy

- `feature/*` → `develop`: squash merge, 1 approval.
- `develop` → `main`: merge commit (preserves sprint history), 2 approvals, done at sprint end.

---

## 3. Daily Workflow

### Standup format (15 minutes max)

**When:** Daily, agreed fixed time. If a team member cannot attend (university schedule), they post async in the team channel before the standup.

**Three questions per person (2 minutes each, 8 minutes total + 7 minutes for blockers):**

1. **What did I finish since last standup?** — Reference the US-XXX ID.
2. **What am I working on today?** — Reference the US-XXX ID.
3. **Am I blocked on anything?** — If yes, name the person or dependency. Resolution assigned immediately.

### Async fallback

If a team member cannot attend standup due to university schedule:

1. Post the three answers in the team channel (Slack/Discord/WhatsApp) **before** the standup time.
2. If blocked, tag the blocking person directly — do not wait until they see it.
3. Sprint lead reads the async update at the beginning of standup and relays to the team.

### Sprint ceremonies

| Ceremony | When | Duration | Purpose |
|---|---|---|---|
| Sprint Planning | Day 1 of sprint | 1 hour | Assign stories, clarify acceptance criteria, identify blockers |
| Daily Standup | Every day | 15 min | Sync, unblock |
| Sprint Review | Last day of sprint | 30 min | Demo working software, get feedback |
| Sprint Retro | After review | 30 min | What went well, what to improve, one action item |

---

## 4. Contract-First Rule

> **No service implementation starts until its API contract and event schemas are published.**

### What this means concretely:

1. Before Sprint 1 coding begins, all endpoints in [service-contracts.md](../docs/phases/phase-3/service-contracts.md) and all events in [event-schemas.md](../docs/phases/phase-3/event-schemas.md) are reviewed and accepted by the team.
2. Every cross-service dependency must have a **mock** before Sprint 1 ends:
   - Person 2 needs Identity auth → Person 1 provides a mock auth endpoint or a shared test JWT.
   - Person 3 needs Reservation data → Person 2 provides a stub `/v1/reservations` that returns a hardcoded response.
   - Person 4 needs PaymentAuthorized event → Person 3 provides a test RabbitMQ publisher script.
3. Mocks live in `src/test/resources/mocks/` of each service or in a shared `mocks/` directory at repo root.
4. If a contract changes after implementation begins, the person changing it must:
   - Notify all consumers in the team channel immediately.
   - Update `service-contracts.md` or `event-schemas.md` in the same PR.
   - Downstream services update within 24 hours.

---

## 5. Escalation Rule

> **If blocked for more than 2 hours, escalate immediately. Do not wait for standup.**

### Escalation protocol:

1. **0–30 min:** Try to unblock yourself (read docs, search, experiment).
2. **30 min–2 hours:** Ask in the team channel. Tag the person who owns the blocking service.
3. **2 hours:** Direct message / call the blocking person. If unreachable, tag the sprint lead.
4. **4 hours:** Sprint lead reassigns work or creates a bypass (mock, stub, skip to next story).

### What counts as blocked:

- Waiting for an API endpoint that doesn't exist yet.
- Waiting for a PR review.
- Waiting for a contract or schema decision.
- Environment issue (Docker, database, RabbitMQ) that you cannot fix alone.

### What does NOT count as blocked:

- "I don't know how to implement this" → research is your job.
- "The tests are hard to write" → that's the work.

---

## 6. References

- **Definition of Done:** [docs/phases/phase-4/definition-of-done.md](../docs/phases/phase-4/definition-of-done.md) — every deliverable must meet these criteria.
- **Documentation index:** [INDEX.md](../INDEX.md) — if it's not in the index, it does not exist.
- **Product backlog:** [docs/phases/phase-4/product-backlog.md](../docs/phases/phase-4/product-backlog.md) — the single source of truth for what to build and in what order.
- **Implementation guide:** [project/implementation-guide.md](implementation-guide.md) — the 45-day plan with per-sprint deliverables.
