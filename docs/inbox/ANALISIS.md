# OrionTicket
Ticket sales system focusing on massive events, with high concurrency and anti-fraud measures.
## DevTodo un Poco
Software solutions startup, focusing on custom software development for businesses.
## Objective of this discovery
The client requested something equivalent to an eTicket-type ticket sales platform, with a non-negotiable restriction: **it must be developed in microservices**.
The request is still ambiguous. Before talking about frontend, backend, database, cloud, queues, Kubernetes, or language, we first need to answer business, domain, operation, risk, and architecture questions. This document exists for that purpose.
## Golden rule
**“Microservices” is an architectural constraint, not a product definition.**
It does not by itself solve tenancy, concurrency, resilience, seat reservations, consistency, payments, fraud, refunds, observability, support, or business operations.
## What should come out of this discovery
By answering this questionnaire, we should be able to produce:
1. A precise product definition.
2. The real scope of the MVP and what is left out.
3. The actors, modules, and critical flows.
4. The bounded contexts and microservice candidates.
5. The real non-functional requirements.
6. The main project risks.
7. A defensible initial architecture.
8. A phased roadmap.
## How to answer this document
- Do not answer with "yes" or "no" when the topic requires context.
- Whenever possible, include real operation examples.
- Distinguish between **MVP**, **desired version**, and **future vision**.
- If something is undefined, mark it as **pending decision**, do not invent it.
- Each answer must help reduce ambiguity, not increase it.
---
## Block 1 — Vision, product, and business model
1. **What exactly is OrionTicket?** A platform for a single business, a white-label solution for multiple organizers, an event marketplace, or a combination?
    
    *Impact:* defines tenancy, data isolation, branding, panels, billing, and overall complexity.
    
    *Answer:* white-label solution for multiple organizers. Organizer with multiple events (marketplace).
    
2. **Who pays to use the platform?** The final buyer, the organizer, both, or is there a per-transaction commission?
    
    *Impact:* affects pricing, billing, reconciliation, and financial model.
    **Answer:** organizer pays for using the platform.
    
3. **What is the main business objective in the first stage?** Validate the market, sell tickets quickly, offer SaaS to organizers, operate own events, or build scalable infrastructure from the start?
    
    *Impact:* defines architectural priorities and the real scope of the MVP.
    *Answer:* validate the market and build scalable infrastructure from the start.
    
4. **What concrete problem does the client want to solve that is currently not well solved by existing alternatives?**
    
    *Impact:* helps identify the real value proposition and avoids building "just another generic Ticketmaster".
    *Answer:* manage real concurrency in ticket sales, and have a resilient system; currently, other platforms allow sales to resellers, which generates fraud and access control problems.
    
5. **What will be the competitive differentiator?** For example: better UX, lower commissions, more control for organizers, anti-fraud, digital tickets, multi-venue, white-label, integrations, analytics, or regional focus.
    
    *Impact:* changes functional and non-functional priorities.
    
    *Answer:* lower transaction cost, more control for organizers.
    
6. **What is the definition of project success in 3, 6, and 12 months?**
    
    *Impact:* forces grounding of KPIs and avoids technical decisions without business criteria.
    *Answer:* ROI starts to be noticed in the first year, and in 3 years it is expected to have a solid and recognized client base in the market.
---
## Block 2 — Market, scope, and multi-tenancy
1. **In which countries or regions will the platform initially operate?**
    
    *Impact:* affects currency, languages, taxes, gateways, regulation, formats, and time zone.
    *Answer:* Guatemala.
    
2. **Will there be one or multiple organizers from the first version?**
    
    *Impact:* defines whether the system is single-tenant, multi-tenant, or hybrid.
    *Answer:* 1 organizer.
    
3. **Will each organizer have its own panel, catalog, branding, rules, and reports, or will everything live under a central operation?**
    
    *Impact:* affects authentication, authorization, data partitioning, and backoffice design.
    *Answer:* Each organizer will have its own panel, catalog, branding, rules, and reports.
    
4. **Must the platform allow multiple brands or domains per organizer?**
    
    *Impact:* changes frontend architecture, configuration, security, deployment, and administration.
    *Answer:* No.
    
5. **Will tickets be sold only for own events or also for third parties?**
    
    *Impact:* defines contracts, approval cycles, reconciliation, and publishing rules.
    *Answer:* Only own events.
    
6. **How configurable should the platform be per client or organizer?**
    
    *Impact:* helps decide between configuration by metadata or specific development per client.
    *Answer:* low, the client wants something they can use directly.
---
## Block 3 — Event and ticket domain
1. **What types of events will the system support?** For example: concerts, theater, sports, conferences, festivals, recurring events, VIP experiences, seatless events, hybrid events, or streaming.
    
    *Impact:* changes the domain model and critical modules.
    *Answer:* All types of events.
    
2. **Can an event have multiple dates or performances?**
    
    *Impact:* defines entity hierarchy: event, session, performance, venue, and availability.
    *Answer:* Yes.
    
3. **Will each event have a seating map or will it be general admission?**
    
    *Impact:* this question radically changes the technical problem.
    *Answer:* Both.
    
4. **If there are seats, are they assigned manually, automatically, or does the user choose the exact seat?**
    
    *Impact:* defines reservation complexity, locking, UX, and consistency.
    *Answer:* The user chooses the exact seat.
    
5. **Will there be zones, sections, rows, numbered seats, tables, boxes, or packages?**
    
    *Impact:* affects inventory, pricing, and availability model.
    *Answer:* Yes, zones, sections, rows, numbered seats, tables, boxes, or packages.
    
6. **Will only individual tickets be sold or also combos, VIP packages, add-ons, merchandising, or additional services?**
    
    *Impact:* defines catalog, pricing, checkout, and business rules.
    *Answer:* Only individual tickets.
    
7. **What attributes must a ticket have?** For example: type, price, currency, zone, benefits, restrictions, QR, status, holder, transferability, access policy, batch, associated promotion.
    
    *Impact:* helps to correctly model the ticket aggregate.
    *Answer:* Type, price, currency, zone, benefits, restrictions, QR, status, holder, transferability, access policy, batch, associated promotion.
    
8. **Can a ticket change status after being issued?** Example: reserved, pending payment, paid, issued, canceled, transferred, refunded, invalidated, used.
    
    *Impact:* defines state machine, auditing, and domain events.
    *Answer:* Yes, reserved, pending payment, paid, issued, canceled, transferred, refunded, invalidated, used.
    
9. **Is support needed for presale, sales phases, batches, quotas, promotional codes, or restricted access?**
    
    *Impact:* changes pricing, availability, and temporal rules.
    *Answer:* Yes, presales, sales phases, batches, quotas, promotional codes, or restricted access.
    
10. **Will official resale, ticket transfers, or change of holder be allowed?**
    
    *Impact:* introduces an additional level of legal, operative, and anti-fraud complexity.
    *Answer:* Depends on the situation.
---
## Block 4 — Purchase flow and end-user experience
1. **How does the user discover events?** Main landing page, search engine, filters, categories, city, date, organizer, external campaigns?
    
    *Impact:* defines need for search, SEO, catalogs, and recommendations.
    *Answer:* Main landing page, search engine, filters, categories, city, date, organizer, external campaigns.
    
2. **Must the user register before buying or can they buy as a guest?**
    
    *Impact:* affects conversion, identity, anti-fraud, and purchase experience.
    *Answer:* Must register first.
    
3. **Should multi-event cart be supported or one purchase per event?**
    
    *Impact:* changes checkout, pricing, and order process consistency.
    *Answer:* One purchase per event.
    
4. **How long does a reservation last before expiring if the user does not pay?**
    
    *Impact:* defines expiration, jobs, events, and inventory policies.
    *Answer:* 10 minutes.
    
5. **What happens if two users try to buy the same seat at the same time?**
    
    *Impact:* this is a critical concurrency and consistency question.
    *Answer:* The first one to pay gets the seat.
    
6. **What is the exact checkout flow?** Selection, reservation, identification, payment, confirmation, issuance, and delivery.
    
    *Impact:* allows correct modeling of services and their contracts.
    *Answer:* Selection, reservation, identification, payment, confirmation, issuance, and delivery.
    
7. **How will the ticket be delivered to the buyer?** Email, digital wallet, dynamic QR, PDF, mobile app, direct download, or multiple channels.
    
    *Impact:* defines notification, issuance, and validation services.
    *Answer:* Email, digital wallet, dynamic QR, PDF, mobile app, direct download.
    
8. **Must there be a buyer portal to view purchases, download tickets, transfer them, or request support?**
    
    *Impact:* adds account, support, and post-sales modules.
    *Answer:* Yes.
9. **What mobile experience is expected?** Responsive web, future mobile app, app from day 1, offline support at access?
    
    *Impact:* affects frontend strategy and entry validation.
    *Answer:* Responsive web.
---
## Block 5 — Internal operation, panels, and backoffice
1. **What internal roles will exist?** Example: super admin, platform operator, organizer, venue staff, door validator, support, finance, marketing.
    
    *Impact:* defines RBAC, permissions, and operational limits.
    *Answer:* Super admin, platform operator, organizer, venue staff, door validator, support, finance, marketing.
    
2. **What can each role do and not do?**
    
    *Impact:* avoids late authorization redesigns.
    *Answer:* Depends on the role.
    
3. **What panels are mandatory from the first version?**
    
    *Impact:* helps distinguish between sales system and business operating system.
    *Answer:* Organizer administration panel and super admin or super user panel.
    
4. **Will the organizer be able to create and publish events themselves or will everything go through central approval?**
    
    *Impact:* defines editorial workflow, moderation, and publication statuses.
    *Answer:* will go through central approval.
    
5. **What must the panel be able to manage?** Events, venues, maps, prices, batches, promotions, orders, users, accesses, staff, refunds, reports, auditing.
    
    *Impact:* helps uncover real backoffice modules.
    *Answer:* Events, venues, maps, prices, batches, promotions, orders, users, accesses, staff, refunds, reports, auditing.
    
6. **Is check-in or door validation needed?**
    
    *Impact:* adds an operational domain separate from the purchase process.
    *Answer:* Yes.
    
7. **Must the validation work strictly in real-time or also with offline/intermittent tolerance?**
    
    *Impact:* defines synchronization strategy, duplication, and access fraud control.
    *Answer:* Both.
    
8. **Is attention to operational incidents required?** For example: ticket resending, change of holder, partial refund, manual reservation release, failed payment support.
    
    *Impact:* this defines a lot of the internal panel and the audit model.
    *Answer:* Yes.
    
9. **Is CRM, marketing automation, or buyer segmentation needed within the platform?**
    
    *Impact:* may change scope and justify separate modules or external integrations.
    *Answer:* No.
---
## Block 6 — Money, payments, refunds, and reconciliation
1. **What payment methods will be supported in the MVP?** Cards, transfers, wallets, referenced cash, QR, regional payments.
    
    *Impact:* defines integrations, reconciliation, and fraud.
    *Answer:* Cards, transfers.
    
2. **Will the charge be made by the platform, the organizer, or a third party?**
    
    *Impact:* affects financial liability, settlement, and accounting design.
    *Answer:* The platform.
    
3. **How will the money be distributed among the platform, organizer, and third parties?**
    
    *Impact:* defines commission engine, settlement, and reports.
    *Answer:* The platform will keep a percentage of the sale.
    
4. **Is automatic reconciliation of payments and settlements needed?**
    
    *Impact:* might require a separate financial bounded context.
    *Answer:* Yes.
    
5. **How will refunds, cancellations, and chargebacks work?**
    
    *Impact:* affects statuses, payment integrations, support, and risk.
    *Answer:* determine if the situation requires it. There could be refunds, partial refunds, or perhaps no refunds.
    
6. **Will invoices or tax receipts be issued?**
    
    *Impact:* changes integrations and legal requirements.
    *Answer:* No (for now).
    
7. **Are there variable commission rules per organizer, event, zone, payment method, or channel?**
    
    *Impact:* adds complexity to pricing and settlement.
    *Answer:* No.
    
8. **What financial reports are mandatory for operations and clients?**
    
    *Impact:* defines analytical model, exports, and data precision.
    *Answer:* Sales reports, commission reports, refund reports, chargeback reports.
---
## Block 7 — Risk, fraud, security, and compliance
1. **What level of fraud is expected or wanted to be prevented?** Bots, mass purchases, QR duplication, unauthorized resale, promotion abuse, account theft.
    
    *Impact:* heavily changes security and experience.
    *Answer:* Medium fraud level is expected, especially bots, mass purchases, unauthorized resale.
    
2. **Is a virtual queue, rate limiting, or anti-bot mechanisms needed for high demand?**
    
    *Impact:* defines perimetral architecture and protection strategy.
    *Answer:* Yes.
    
3. **What sensitive data will the system store?**
    
    *Impact:* defines security, encryption, auditing, and compliance.
    *Answer:* Personal user data, payment data, event data, ticket data.
    
4. **What legal or regulatory requirements apply?** Data protection, sales terms, return policies, tax regulations, marketing consent.
    
    *Impact:* affects functional design and data governance.
    *Answer:* Data protection, sales terms, return policies, tax regulations, marketing consent.
    
5. **Is full traceability of sensitive changes and actions required?**
    
    *Impact:* justifies technical and functional auditing right from the start.
    *Answer:* Yes.
    
6. **What incidents are unacceptable for the business?** Example: seat overbooking, double issuance, crash at launch, duplicate validation at the door, lost orders.
    
    *Impact:* helps define real resilience priorities.
    *Answer:* Seat overbooking, double issuance, crash at launch, duplicate validation at the door, lost orders.
---
## Block 8 — Scalability, availability, and non-functional requirements
1. **What volume is expected under normal conditions and peak times?** Concurrent users, transactions per minute, massive events, check-ins per minute.
    
    *Impact:* without this, serious design is impossible.
    *Answer:* under normal conditions, 5,000 concurrent users; at peak, 30,000 concurrent users. Transactions per minute under normal conditions, 100; at peak, 1,000.
    
2. **What is the worst expected peak?** Major artist presale, sports final, simultaneous launch, high-demand festivals.
    
    *Impact:* changes scaling and protection strategy.
    *Answer:* Major artist presale, sports final.
    
3. **What SLA or availability does the business expect?**
    
    *Impact:* helps decide level of redundancy and operational complexity.
    *Answer:* 99.9%.
    
4. **What latency is acceptable in purchase, issuance, and validation?**
    
    *Impact:* influences distributed architecture, caches, and experience.
    *Answer:* In purchase, 1 second. In issuance, 500ms. In validation, 100ms.
    
5. **How long can the system be down without critical impact?**
    
    *Impact:* helps prioritize recovery, backups, and continuity design.
    *Answer:* 1 hour.
    
6. **Which data must be consistent in real-time, and which tolerate eventual consistency?**
    
    *Impact:* this a central question in microservices.
    *Answer:* In real-time: ticket inventory, order status, access validations, payments. Eventual consistency: reports, metrics, logs.
    
7. **Is advanced observability needed from day 1?** Structured logs, distributed tracing, business metrics, alerts, operational dashboards.
    
    *Impact:* in microservices, this is not optional, it's basic infrastructure.
    *Answer:* Yes.
---
## Block 9 — Integrations and ecosystem
1. **What systems must OrionTicket integrate with?** Gateways, ERPs, CRMs, email/SMS/WhatsApp, analytics, billing, biometrics, access control hardware, partners.
    
    *Impact:* defines service boundaries, events, contracts, and real backlog.
    *Answer:* Gateways, email/SMS/WhatsApp, analytics.
    
2. **Is a public API or partner API needed?**
    
    *Impact:* adds versioning, security, and external governance.
    *Answer:* Yes.
    
3. **Will it integrate with venue maps or external seating software?**
    
    *Impact:* can radically modify build vs buy.
    *Answer:* No.
    
4. **Is import or migration from previous systems required?**
    
    *Impact:* adds early data complexity and compatibility.
    *Answer:* No.
    
5. **Will there be external sales channels besides the main site?** Physical points, kiosks, call center, third-party apps, affiliates, marketplaces.
    
    *Impact:* demands a reusable transactional core, not just a web page.
    *Answer:* No.
---
## Block 10 — Project, team, and implementation restrictions
1. **What does "must be in microservices" mean for the client?** Is it a real technical requirement, a corporate policy, a scalability expectation, or a decision already made without clear reasons?
    
    *Impact:* allows questioning assumptions and grounding expectations.
    *Answer:* expectation of scalability and resilience.
    
2. **Does the client expect independently deployed microservices from day 1 or accept a modular monolith with clear boundaries evolving to physical services?**
    
    *Impact:* defines implementation strategy and risk.
    *Answer:* independently deployed microservices from day 1.
    
3. **What real capabilities does the team have?** Backend, frontend, DevOps, QA, security, observability, data, support.
    
    *Impact:* conditions viable architecture, not just the idealized one.
    *Answer:* 2 backend developers, 1 frontend developer, 1 DevOps developer, 1 QA, 1 security, 1 observability, 1 data, 1 support.
    
4. **Who will operate the platform in production?**
    
    *Impact:* microservices without mature operational capacity usually increase risk.
    *Answer:* The development team.
    
5. **What is the real budget and the real deadline?**
    
    *Impact:* defines scope, infrastructure, automation, and acceptable debt.
    *Answer:* Budget: $50,000. Deadline: 3 months.
    
6. **How tolerant is the client to technical risk?**
    
    *Impact:* helps decide how much to build and how much to integrate.
    *Answer:* Moderate.
    
7. **Is a quick market release prioritized over a strong technical foundation for the long term?**
    
    *Impact:* changes the MVP strategy, deployment, and technical debt.
    *Answer:* Strong technical foundation for the long term.
    
8. **What things must absolutely exist in the first delivery and what can wait?**
    
    *Impact:* defines real MVP and subsequent backlog.
    *Answer:* event management, ticket sales, ticket validation, client management, notifications, payments, reports.
    
9. **What things would be desirable but not mandatory in the first phase?**
    
    *Impact:* protects the project from scope creep.
    *Answer:* custom panel for clients, social media integrations, payment gateway integrations, billing system integrations, partner system integrations.
---
## Block 11 — Architectural questions that should not be skipped if the path to microservices is taken
1. **What would be the natural bounded contexts of the domain?** Example: Identity, Catalog, Event Management, Seating/Inventory, Pricing, Orders, Payments, Ticket Issuance, Access Control, Notifications, Reporting.
    
    *Impact:* this must come from the business, not from a technical trend.
    *Answer:* Identity, Catalog, Event Management, Seating/Inventory, Pricing, Orders, Payments, Ticket Issuance, Access Control, Notifications, Reporting.
    
2. **Who will own each critical piece of data?**
    
    *Impact:* avoids duplication, coupling, and conflicts between services.
    *Answer:* we are not sure.
    
3. **Which flows need strong transactions and which can be solved with asynchronous events?**
    
    *Impact:* defines consistency, orchestration, and sagas.
    *Answer:* inventory, orders, payments, issuance, and validation.
    
4. **How will idempotency be handled in payments, orders, issuance, and validation?**
    
    *Impact:* mandatory for real resilience.
    *Answer:* unique key formed from transaction data, client data, event data, ticket data, order data; essentially the exact date.
    
5. **What domain events should exist?** Example: EventPublished, SeatHeld, HoldExpired, OrderCreated, PaymentAuthorized, PaymentFailed, TicketIssued, TicketRefunded, TicketCheckedIn.
    
    *Impact:* helps design contracts between services.
    *Answer:* all of them, including notifications.
    
6. **Which services require synchronous communication and which must be decoupled via messaging?**
    
    *Impact:* avoids a fragile mesh of calls between microservices.
    *Answer:* we are not sure.
    
7. **What part of the system needs high consistency under extreme concurrency?**
    
    *Impact:* normally inventory, reservations, orders, and payments.
    *Answer:* inventory, orders, payments, issuance, and validation.
    
8. **What observability and troubleshooting strategy is expected for distributed environments?**
    
    *Impact:* without this, operating microservices will be a pain.
    *Answer:* we are not sure.
    
9. **What versioning policy will be followed for APIs, contracts, and events?**
    
    *Impact:* essential for independent evolution.
    *Answer:* semantic versioning.
    
10. **What is the boundary between core business services and cross-cutting support services?**
    
    *Impact:* helps avoid mixing business with technical concerns.
    *Answer:* we are not sure.
---
## Block 12 — Final synthesis that must be answered after the discovery
Once this questionnaire is answered, we should be able to precisely draft:
1. **Product definition in a single sentence.**
2. **Primary and secondary users.**
3. **Business and monetization model.**
4. **Exact MVP and explicit exclusions.**
5. **Critical business flows.**
6. **Main operational and technical risks.**
7. **Priority non-functional requirements.**
8. **Bounded contexts and microservice candidates.**
9. **Mandatory integrations.**
10. **Phased roadmap.**
---
## Recommended deliverables after answering this questionnaire
1. Vision and scope document.
2. Map of actors and roles.
3. Prioritized list of use cases.
4. Initial domain model.
5. Event storming or business events map.
6. Bounded contexts and initial microservices proposal.
7. Non-functional requirements.
8. Risks and assumptions.
9. Phased MVP.
10. Defensible initial architecture.

