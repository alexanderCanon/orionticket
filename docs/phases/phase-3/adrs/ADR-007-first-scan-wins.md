# ADR-007: First-Scan-Wins for Offline Sync Conflicts

| Field | Value |
|---|---|
| **Date** | 2026-04-13 |
| **Status** | ACCEPTED |

## Context

Venues with intermittent connectivity require offline Validation capability. A Door Validator device may scan Tickets while disconnected and queue the results locally. When the device reconnects, queued ValidationRecords are synced to the server. A conflict arises when the same Ticket was scanned by an online validator (or another offline device) in the interim.

## Options Considered

1. **Last-write-wins** — The most recent scan overrides previous records. Simple but allows fraudulent second entry.
2. **First-scan-wins** — The first recorded scan (chronologically) is accepted; all subsequent scans for the same Ticket are rejected as conflicts.
3. **Manual resolution** — Conflicts queued for Venue Staff to resolve case-by-case.

## Decision

**First-scan-wins.** The first ValidationRecord with `result: SUCCEEDED` for a given ticketId is authoritative. All subsequent scans are `result: FAILED, failureReason: ALREADY_USED, conflictDetected: true`.

## Justification

- Decided in Phase 1 (aggregate-definitions.md — Access Control): "conflictDetected ← true if first-scan-wins rule triggered."
- Duplicate QR validation is a zero-tolerance incident (discovery.md Block 7.6). The system must never accept the same Ticket twice.
- First-scan-wins is the only conflict resolution strategy that guarantees zero duplicate entries.
- Conflicts detected during sync trigger `ConflictDetected` event for audit and operational awareness (domain-events-map.md — Access Control).

## Consequences

- **Positive:** Zero duplicate entries guaranteed. Conflict resolution is deterministic and automated. Full audit trail via ConflictDetected events.
- **Negative:** An offline validator may grant entry locally but the entry is retroactively denied during sync. This creates a UX gap — the person is already inside the venue.
- **Mitigation:** Venue Staff receive conflict alerts immediately upon sync. Operational protocol must be defined for handling retroactive denials at the door.

## Source

- aggregate-definitions.md — Access Control (conflictDetected field)
- domain-events-map.md — Access Control (ConflictDetected event)
- discovery.md — Block 5.6, Block 7.6, Block 12.5
