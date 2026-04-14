# Ubiquitous Language Glossary

| # | Concept | Term | Notes |
|---|---------|------|-------|
| 1 | Parent event concept | Event | e.g. "Coldplay Guatemala" |
| 2 | Specific date/time of an event | Date | Child of Event |
| 3 | Seat locked, pending payment | Reservation | Expires in 10 min, lives in Inventory |
| 4 | Purchase intent and lifecycle | Order | Created when buyer proceeds to payment |
| 5 | Document granting access | Ticket | Issued after payment, with or without a seat |
| 6 | Physical/logical venue position | Seat | Only exists in mapped venues |
| 7 | Multi-tenant client | Organizer | Business and technical layers |
| 8 | Door entry action | Validation | Performed at entry point |
| 9 | Physical layout grouping | Zone / Section / Row | Flexible hierarchy, configured per venue |
| 10 | Time-limited priced ticket group | Batch | e.g. "Early bird — 500 tickets at $20" |
| 11 | Discount rule and activation code | Promotion | Code is just its activation mechanism |
| 12 | Total seats/tickets, fixed | Capacity | Set at Event/Date creation |
| 13 | Remaining tickets, dynamic | Availability | Calculated, never persisted |
| 14 | Operational coordinator at venue | Venue Staff | Manages Door Validators |
| 15 | Staff member scanning QRs | Door Validator | Restricted permissions, scans only |
| 16 | App/device doing the scan | Validator | Technical term, not a person |
| 17 | Platform's cut per transaction | Service Fee | Retained before Payout |
| 18 | Payment to organizer after fee | Payout | Triggered after Order confirmed |
| 19 | Record of sensitive actions | Audit Log | Per-action, append-only |
| 20 | Event becoming visible to buyers | Release | Organizer submits → Operator releases |
| 21 | Entry rule on a Ticket | Access Policy | Defines when/how many times entry is allowed. |