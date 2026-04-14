Identity — manages who users are and what they can do
Event Management — manages the lifecycle of events, dates, venues, and their public visibility
Seating / Inventory — manages capacity, seat state, reservations, and batches
Orders — manages the checkout process, price resolution, and order lifecycle
Payments — manages payment processing, service fee collection, and organizer payouts
Ticket Issuance — manages ticket generation, delivery, and post-issuance state
Access Control — manages entry validation, offline sync, and conflict resolution
Notifications — manages delivery of messages to users across all channels
Reporting — manages aggregated data, financial reports, and exports for all roles

What changed from DISCOVERY.md and why
Original    Decision                                     Reason
Catalog     Dropped → read model inside Event Management  No own data, no own rules
Pricing     Dropped → absorbed by Orders                  No events of its own after Batches moved
Batches     Moved from Pricing → Seating/Inventory        Inventory concept, not a price concept

