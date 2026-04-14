# Aggregates

## Seating/Inventory - Final Agreggate

Aggregate Root: Seat
  ├── seatId
  ├── eventId
  ├── dateId
  ├── zone / section / row (nullable for general admission)
  ├── type: MAPPED | GENERAL_ADMISSION
  ├── status: AVAILABLE | RESERVED | SOLD | BLOCKED
  ├── accessPolicy
  ├── Reservation (child entity)
  │     ├── reservationId
  │     ├── buyerId
  │     ├── expiresAt
  │     └── status: ACTIVE | EXPIRED | RELEASED
  └── Batch (child entity)
        ├── batchId
        ├── name
        ├── price
        ├── currency
        ├── capacity
        ├── sold                ← incremented atomically with Reservation
        ├── status: SCHEDULED | ACTIVE | EXHAUSTED | EXPIRED
        └── scheduledStartAt

type: MAPPED | GENERAL_ADMISSION replaces the Seat vs Slot distinction — one unified concept, two behaviors.

## Orders — Aggregate Design
Orders owns checkout lifecycle, price resolution, and promotion application. Less concurrent pressure than Inventory, but more business rules.

Aggregate Root: Order
  ├── orderId
  ├── buyerId
  ├── eventId
  ├── dateId
  ├── reservationId              ← reference only, Order dies when Reservation expires
  ├── status: CREATED | PAYMENT_INITIATED | CONFIRMED | EXPIRED | FAILED
  ├── lineItems[]
  │     ├── lineItemId
  │     ├── seatId
  │     ├── batchPrice
  │     └── quantity
  ├── subtotal
  ├── promotionId                ← reference only, applied at Order level
  ├── promotionDiscount
  ├── serviceFee
  ├── total
  ├── currency
  └── createdAt

## Payments — Aggregate Design
Payments owns the gateway interaction, service fee collection, and organizer payouts. It never owns Order or Ticket data — it only knows money moved.

Aggregate Root: Payment
  ├── paymentId
  ├── orderId                    ← reference only
  ├── buyerId
  ├── amount
  ├── serviceFee
  ├── currency
  ├── method: CARD | TRANSFER
  ├── status: INITIATED | AUTHORIZED | FAILED
  ├── gatewayReference           ← external transaction ID
  ├── idempotencyKey
  └── createdAt

Aggregate Root: Payout
  ├── payoutId
  ├── organizerId
  ├── eventId
  ├── dateId
  ├── grossAmount
  ├── serviceFeeTotal
  ├── netAmount
  ├── status: PENDING | PROCESSED | FAILED
  ├── retryCount                 ← max 1 automatic retry
  ├── triggeredAt
  └── processedAt

## Ticket Issuance — Aggregate Design
Ticket Issuance owns the issued document and its post-issuance state. It fires after PaymentAuthorized and never touches Orders or Inventory again.

Aggregate Root: Ticket
  ├── ticketId
  ├── orderId                    ← reference only
  ├── buyerId
  ├── eventId
  ├── dateId
  ├── seatId                     ← null for general admission
  ├── type: MAPPED | GENERAL_ADMISSION
  ├── holderName
  ├── qrCode                     ← dynamic, regenerates every 2 minutes
  ├── qrExpiresAt                ← TTL control
  ├── accessPolicy
  ├── status: ISSUED | CANCELED | INVALIDATED | USED
  ├── deliveryChannels[]         ← EMAIL | PDF | QR | WALLET | DOWNLOAD
  ├── deliveredAt
  └── issuedAt

## Access Control — Aggregate Design
Access Control owns the validation record and offline sync state. It never modifies a Ticket — it only records what happened at the door and fires TicketInvalidated when fraud is detected.

Aggregate Root: ValidationRecord
  ├── validationId
  ├── ticketId                   ← reference only, one-way
  ├── validatorDeviceId
  ├── eventId
  ├── dateId
  ├── attemptedAt
  ├── result: SUCCEEDED | FAILED
  ├── failureReason              ← ALREADY_USED | WRONG_EVENT | EXPIRED | INVALIDATED
  ├── isOffline                  ← was device offline at scan time?
  ├── syncedAt                   ← null until reconciled
  └── conflictDetected           ← true if first-scan-wins rule triggered

## Identity — Aggregate Design
Identity owns who users are and what they can do. Two clear aggregates here.

Aggregate Root: User
  ├── userId
  ├── email
  ├── passwordHash
  ├── fullName
  ├── phone
  ├── status: ACTIVE | SUSPENDED | UNVERIFIED
  ├── roleId                     ← reference to Role
  ├── organizerId                ← null for platform-level users
  └── createdAt

Aggregate Root: Role
  ├── roleId
  ├── name
  └── permissions[]              ← no user list here

## Notifications — Aggregate Design
Simple. Notifications is infrastructure — it reacts to events, delivers messages, records outcomes.

Aggregate Root: Notification
  ├── notificationId
  ├── recipientId                ← userId
  ├── channel: EMAIL | SMS | WHATSAPP
  ├── templateId                 ← what message to send
  ├── payload                    ← dynamic data for template
  ├── status: PENDING | DISPATCHED | DELIVERED | FAILED
  ├── retryCount                 ← Support triggers manual resend
  ├── triggeredBy                ← domain event that caused this
  └── createdAt

## Reporting — Aggregate Design
Reporting has no traditional aggregates — it's a read model. But it does own its projections, and those need structure.

# Cross-cutting infrastructure
AuditLog
  ├── logId
  ├── actorId
  ├── actorRole
  ├── action
  ├── targetEntity
  ├── targetId
  ├── previousState              ← JSON snapshot
  ├── newState                   ← JSON snapshot
  └── occurredAt

# Reporting — read models only
Read Model: SalesReport
  ├── reportId
  ├── organizerId
  ├── eventId
  ├── dateId
  ├── totalTicketsSold
  ├── totalRevenue
  ├── totalServiceFees
  ├── totalPayouts
  └── generatedAt

Read Model: CommissionReport
  ├── reportId
  ├── organizerId
  ├── periodStart
  ├── periodEnd
  ├── totalServiceFees
  └── generatedAt

# Identity
User                           ← owns roleId reference
Role                           ← owns permissions, no user list

# Event Management
Event                          ← owns Dates
Date                           ← child of Event, owns schedule

# Seating / Inventory
Seat                           ← owns Reservation + Batch atomically
  └── Reservation              ← child entity, dies with Seat hold
  └── Batch                    ← child entity, sold count atomic

# Orders
Order                          ← owns LineItems, dies with ReservationExpired
  └── LineItem                 ← child entity

# Payments
Payment                        ← owns gateway interaction
Payout                         ← owns organizer settlement, 1 auto retry

# Ticket Issuance
Ticket                         ← owns dynamic QR (2min TTL), post-issuance state

# Access Control
ValidationRecord               ← immutable, one-way reference to Ticket

# Notifications
Notification                   ← owns delivery state, manual retry by Support

# Reporting
SalesReport                    ← read model, projection only
CommissionReport               ← read model, projection only

# Cross-cutting
AuditLog                       ← written by every service, owned by none