Days 1–7    Documentation complete (Phase 2, 3, 4)
            Team reads everything, no ambiguity allowed

Days 8–17   Sprint 1 — Foundation
            Identity service (auth, roles, JWT)
            Event Management (create, submit, release flow)
            Basic organizer panel (event CRUD)

Days 18–27  Sprint 2 — Core transaction
            Seating/Inventory (seats, reservations, batches)
            Orders (checkout, price resolution)
            Payments (gateway integration)

Days 28–37  Sprint 3 — Ticket lifecycle
            Ticket Issuance (QR generation, delivery)
            Notifications (email, basic SMS)
            Buyer portal (view purchases, download tickets)

Days 38–45  Sprint 4 — Operations & stabilization
            Access Control (real-time validation)
            Basic Reporting (sales report)
            Bug fixes, integration testing, demo prep

What gets cut from the full spec:

Offline validator sync → demo with real-time only
Dynamic QR regeneration → static QR acceptable for MVP demo
Payout automation → manual trigger by Finance is enough
Full audit log → basic logging only
Anti-fraud (rate limiting, virtual queue) → document as v2