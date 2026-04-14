# Data Ownership Map

> **Phase:** 3 — Architecture  
> **Source:** `docs/phases/phase-1/aggregate-definitions.md`, `docs/phases/phase-1/bounded-context-diagrams.md`, `docs/phases/phase-0/discovery.md` Block 11  
> **Rule:** Each bounded context owns its data exclusively. No shared databases. Cross-service reads happen via synchronous API calls or asynchronous event projections only.

---

## Identity

| Aggregate | Owning Service | Owned Entities | External References (IDs only) | Consumers | Read Mechanism |
|---|---|---|---|---|---|
| **User** | Identity | User (email, passwordHash, fullName, phone, status, createdAt) | roleId → Role (same service), organizerId → Organizer (Event Management) | Orders, Ticket Issuance, Notifications, Reporting | API — synchronous lookup by userId |
| **Role** | Identity | Role (name, permissions[]) | — | All services (authorization checks) | API — synchronous lookup by roleId |

---

## Event Management

| Aggregate | Owning Service | Owned Entities | External References (IDs only) | Consumers | Read Mechanism |
|---|---|---|---|---|---|
| **Event** | Event Management | Event (name, description, category, status, branding), Date (datetime, Venue, Capacity, schedule) | organizerId → User (Identity) | Seating/Inventory, Orders, Access Control, Ticket Issuance, Reporting, Notifications | API — synchronous read of Event/Date details. Event projection via EventCreated, EventReleased, EventCanceled |
| **Catalog read model** | Event Management | Public-facing listing projection of Event + Date data | — | Buyer (public API) | API — synchronous read-only endpoint |

---

## Seating / Inventory

| Aggregate | Owning Service | Owned Entities | External References (IDs only) | Consumers | Read Mechanism |
|---|---|---|---|---|---|
| **Seat** | Seating/Inventory | Seat (zone, section, row, type, status, accessPolicy), Reservation (buyerId, expiresAt, status), Batch (name, price, currency, capacity, sold, status, scheduledStartAt) | eventId → Event (Event Management), dateId → Date (Event Management), buyerId → User (Identity) | Orders (reservationId reference), Ticket Issuance (seatId reference), Access Control (via Ticket), Reporting | API — synchronous seat availability and hold. Event projection via ReservationCreated, ReservationExpired, BatchActivated, BatchExhausted |

---

## Orders

| Aggregate | Owning Service | Owned Entities | External References (IDs only) | Consumers | Read Mechanism |
|---|---|---|---|---|---|
| **Order** | Orders | Order (status, subtotal, promotionDiscount, serviceFee, total, currency, createdAt), LineItem (batchPrice, quantity) | buyerId → User (Identity), eventId → Event (Event Management), dateId → Date (Event Management), reservationId → Reservation (Seating/Inventory), seatId → Seat (Seating/Inventory), promotionId → Promotion (Seating/Inventory) | Payments (orderId reference), Ticket Issuance (orderId reference), Notifications, Reporting | API — synchronous read of Order status. Event projection via OrderCreated, OrderExpired |

---

## Payments

| Aggregate | Owning Service | Owned Entities | External References (IDs only) | Consumers | Read Mechanism |
|---|---|---|---|---|---|
| **Payment** | Payments | Payment (amount, serviceFee, currency, method, status, gatewayReference, idempotencyKey, createdAt) | orderId → Order (Orders), buyerId → User (Identity) | Orders (PaymentAuthorized/PaymentFailed events), Ticket Issuance (PaymentAuthorized trigger), Seating/Inventory (PaymentFailed → release), Notifications, Reporting | Event projection via PaymentAuthorized, PaymentFailed |
| **Payout** | Payments | Payout (grossAmount, serviceFeeTotal, netAmount, status, retryCount, triggeredAt, processedAt) | organizerId → User (Identity), eventId → Event (Event Management), dateId → Date (Event Management) | Reporting (PayoutProcessed/PayoutFailed events), Finance (API read) | API — synchronous read for Finance. Event projection via PayoutGenerated, PayoutProcessed, PayoutFailed |

---

## Ticket Issuance

| Aggregate | Owning Service | Owned Entities | External References (IDs only) | Consumers | Read Mechanism |
|---|---|---|---|---|---|
| **Ticket** | Ticket Issuance | Ticket (holderName, qrCode, qrExpiresAt, accessPolicy, type, status, deliveryChannels[], deliveredAt, issuedAt) | orderId → Order (Orders), buyerId → User (Identity), eventId → Event (Event Management), dateId → Date (Event Management), seatId → Seat (Seating/Inventory) | Access Control (ticketId reference, synchronous QR lookup), Notifications (TicketIssued trigger), Reporting (TicketIssued, TicketCheckedIn projections) | API — synchronous QR lookup for Access Control (100 ms SLA). Event projection via TicketIssued, TicketCanceled, TicketInvalidated |

---

## Access Control

| Aggregate | Owning Service | Owned Entities | External References (IDs only) | Consumers | Read Mechanism |
|---|---|---|---|---|---|
| **ValidationRecord** | Access Control | ValidationRecord (attemptedAt, result, failureReason, isOffline, syncedAt, conflictDetected) | ticketId → Ticket (Ticket Issuance), validatorDeviceId, eventId → Event (Event Management), dateId → Date (Event Management) | Reporting (TicketCheckedIn, ConflictDetected projections) | Event projection via ValidationSucceeded, ValidationFailed, ConflictDetected, ValidatorSynced |

---

## Notifications

| Aggregate | Owning Service | Owned Entities | External References (IDs only) | Consumers | Read Mechanism |
|---|---|---|---|---|---|
| **Notification** | Notifications | Notification (channel, templateId, payload, status, retryCount, triggeredBy, createdAt) | recipientId → User (Identity) | — (terminal service, no downstream consumers) | API — synchronous read for Support manual resend. Event projection via NotificationDispatched, NotificationDelivered, NotificationFailed |

---

## Reporting

| Aggregate | Owning Service | Owned Entities | External References (IDs only) | Consumers | Read Mechanism |
|---|---|---|---|---|---|
| **SalesReport** (read model) | Reporting | SalesReport (totalTicketsSold, totalRevenue, totalServiceFees, totalPayouts, generatedAt) | organizerId → User (Identity), eventId → Event (Event Management), dateId → Date (Event Management) | Finance, Organizer, Platform Operator, Super Admin | API — synchronous read-only |
| **CommissionReport** (read model) | Reporting | CommissionReport (periodStart, periodEnd, totalServiceFees, generatedAt) | organizerId → User (Identity) | Finance, Super Admin | API — synchronous read-only |

---

## Cross-cutting

| Aggregate | Owning Service | Owned Entities | External References (IDs only) | Consumers | Read Mechanism |
|---|---|---|---|---|---|
| **AuditLog** | None (written by every service) | AuditLog (actorId, actorRole, action, targetEntity, targetId, previousState, newState, occurredAt) | actorId → User (Identity) | Platform Operator, Super Admin (read via centralized query API) | Centralized append-only store. Read via API or log aggregation. |
