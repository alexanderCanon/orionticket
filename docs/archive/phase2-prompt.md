You are a senior systems analyst working on OrionTicket, a white-label ticket sales platform for massive events built in microservices.
Your task is to produce all Phase 2 artifacts based exclusively on the Phase 1 documentation already in this repo. Do not infer, invent, or expand beyond what is defined there.
Read these files first, in order:

docs/phase-0/DISCOVERY.md
docs/phase-1/ubiquitous-language.md
docs/phase-1/domain-events-map.md
docs/phase-1/bounded-contexts.md
docs/phase-1/aggregate-definitions.md

Then produce the following four files:
1. docs/phase-2/actor-role-map.md
Map every actor defined in DISCOVERY.md to the bounded contexts they interact with, and the aggregates they read or modify. Include the permission scope per role from DISCOVERY.md Block 5.
2. docs/phase-2/use-case-catalog.md
Derive one use case per meaningful actor-aggregate interaction. Format each use case as: ID, name, actor, preconditions, main flow, alternative flows, postconditions, domain events fired. Cover all bounded contexts.
3. docs/phase-2/critical-flows.md
Produce sequence diagrams in Mermaid format for these five flows:

Organizer creates and submits event for review
Buyer selects seat, reserves, pays, receives ticket
Reservation expires without payment
Door Validator scans QR — success and failure paths
Offline validator syncs after reconnection

4. docs/phase-2/business-rules.md
Extract every explicit business rule from Phase 0 and Phase 1 into a numbered, plain-language list. Group by bounded context. Each rule must reference the artifact it came from.
Use only the ubiquitous language from docs/phase-1/ubiquitous-language.md. No synonyms, no new terms.