# Event Schemas

> **Phase:** 3 — Architecture  
> **Source:** `docs/phases/phase-1/domain-events-map.md`, `docs/phases/phase-1/aggregate-definitions.md`  
> **Format:** Per event — name, producer, consumers, trigger, full JSON schema.  
> **Constraint:** Field names and types from aggregate definitions. Only events from the domain events map.

---

## Organizer Events

### OrganizerRegistered

| Field | Value |
|---|---|
| **Producer** | Identity |
| **Consumers** | Event Management, Notifications |
| **Trigger** | Organizer completes registration and is created in the system. |

```json
{
  "eventType": "OrganizerRegistered",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "organizerId": "uuid",
    "userId": "uuid",
    "email": "string",
    "fullName": "string",
    "status": "UNVERIFIED"
  }
}
```

### OrganizerApproved

| Field | Value |
|---|---|
| **Producer** | Identity |
| **Consumers** | Event Management, Notifications |
| **Trigger** | Platform Operator or Super Admin approves the Organizer account. |

```json
{
  "eventType": "OrganizerApproved",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "organizerId": "uuid",
    "approvedBy": "uuid",
    "status": "ACTIVE"
  }
}
```

### OrganizerSuspended

| Field | Value |
|---|---|
| **Producer** | Identity |
| **Consumers** | Event Management, Notifications |
| **Trigger** | Super Admin suspends an Organizer account. |

```json
{
  "eventType": "OrganizerSuspended",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "organizerId": "uuid",
    "suspendedBy": "uuid",
    "reason": "string",
    "status": "SUSPENDED"
  }
}
```

---

## Venue Events

### VenueCreated

| Field | Value |
|---|---|
| **Producer** | Event Management |
| **Consumers** | Seating/Inventory |
| **Trigger** | Organizer creates a new Venue for their Events. |

```json
{
  "eventType": "VenueCreated",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "venueId": "uuid",
    "organizerId": "uuid",
    "name": "string",
    "address": "string",
    "capacity": "integer"
  }
}
```

### SeatingMapConfigured

| Field | Value |
|---|---|
| **Producer** | Seating/Inventory |
| **Consumers** | Event Management |
| **Trigger** | Organizer completes Seating Map configuration (Zones, Sections, Rows, Seats). |

```json
{
  "eventType": "SeatingMapConfigured",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "eventId": "uuid",
    "dateId": "uuid",
    "venueId": "uuid",
    "totalSeats": "integer",
    "zones": [
      {
        "zone": "string",
        "sections": [
          {
            "section": "string",
            "rows": [
              {
                "row": "string",
                "seatCount": "integer"
              }
            ]
          }
        ]
      }
    ],
    "generalAdmissionCapacity": "integer | null"
  }
}
```

### SeatingMapUpdated

| Field | Value |
|---|---|
| **Producer** | Seating/Inventory |
| **Consumers** | Event Management |
| **Trigger** | Organizer modifies an existing Seating Map (only before Event is Released). |

```json
{
  "eventType": "SeatingMapUpdated",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "eventId": "uuid",
    "dateId": "uuid",
    "venueId": "uuid",
    "totalSeats": "integer",
    "generalAdmissionCapacity": "integer | null"
  }
}
```

---

## Event Lifecycle Events

### EventCreated

| Field | Value |
|---|---|
| **Producer** | Event Management |
| **Consumers** | Reporting |
| **Trigger** | Organizer creates a new Event. |

```json
{
  "eventType": "EventCreated",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "eventEntityId": "uuid",
    "organizerId": "uuid",
    "name": "string",
    "category": "string",
    "status": "DRAFT"
  }
}
```

### EventSubmittedForReview

| Field | Value |
|---|---|
| **Producer** | Event Management |
| **Consumers** | Notifications |
| **Trigger** | Organizer submits Event for Platform Operator review. |

```json
{
  "eventType": "EventSubmittedForReview",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "eventEntityId": "uuid",
    "organizerId": "uuid",
    "submittedBy": "uuid",
    "status": "UNDER_REVIEW"
  }
}
```

### EventReleased

| Field | Value |
|---|---|
| **Producer** | Event Management |
| **Consumers** | Reporting, Notifications |
| **Trigger** | Platform Operator approves the Event for public visibility. |

```json
{
  "eventType": "EventReleased",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "eventEntityId": "uuid",
    "organizerId": "uuid",
    "approvedBy": "uuid",
    "status": "RELEASED"
  }
}
```

### EventCanceled

| Field | Value |
|---|---|
| **Producer** | Event Management |
| **Consumers** | Seating/Inventory, Orders, Notifications, Reporting |
| **Trigger** | Organizer or Platform Operator cancels an Event. |

```json
{
  "eventType": "EventCanceled",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "eventEntityId": "uuid",
    "organizerId": "uuid",
    "canceledBy": "uuid",
    "reason": "string"
  }
}
```

### DateAdded

| Field | Value |
|---|---|
| **Producer** | Event Management |
| **Consumers** | Seating/Inventory, Reporting |
| **Trigger** | Organizer adds a Date (performance/session) to an Event. |

```json
{
  "eventType": "DateAdded",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "dateId": "uuid",
    "eventEntityId": "uuid",
    "scheduledAt": "ISO-8601",
    "venueId": "uuid",
    "capacity": "integer"
  }
}
```

### DateCanceled

| Field | Value |
|---|---|
| **Producer** | Event Management |
| **Consumers** | Seating/Inventory, Orders, Notifications, Reporting |
| **Trigger** | Organizer or Platform Operator cancels a specific Date. |

```json
{
  "eventType": "DateCanceled",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "dateId": "uuid",
    "eventEntityId": "uuid",
    "canceledBy": "uuid",
    "reason": "string"
  }
}
```

---

## Pricing & Batch Events

### BatchCreated

| Field | Value |
|---|---|
| **Producer** | Seating/Inventory |
| **Consumers** | Reporting |
| **Trigger** | Organizer defines a new Batch for a Date/Event. |

```json
{
  "eventType": "BatchCreated",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "batchId": "uuid",
    "eventEntityId": "uuid",
    "dateId": "uuid",
    "name": "string",
    "price": "decimal",
    "currency": "string",
    "capacity": "integer",
    "scheduledStartAt": "ISO-8601",
    "status": "SCHEDULED"
  }
}
```

### BatchScheduled

| Field | Value |
|---|---|
| **Producer** | Seating/Inventory |
| **Consumers** | Reporting |
| **Trigger** | Batch is persisted with a future start time. |

```json
{
  "eventType": "BatchScheduled",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "batchId": "uuid",
    "eventEntityId": "uuid",
    "dateId": "uuid",
    "scheduledStartAt": "ISO-8601"
  }
}
```

### BatchActivated

| Field | Value |
|---|---|
| **Producer** | Seating/Inventory |
| **Consumers** | Reporting, Notifications |
| **Trigger** | Current time reaches Batch.scheduledStartAt. |

```json
{
  "eventType": "BatchActivated",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "batchId": "uuid",
    "eventEntityId": "uuid",
    "dateId": "uuid",
    "status": "ACTIVE"
  }
}
```

### BatchExhausted

| Field | Value |
|---|---|
| **Producer** | Seating/Inventory |
| **Consumers** | Reporting |
| **Trigger** | Batch.sold reaches Batch.capacity. |

```json
{
  "eventType": "BatchExhausted",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "batchId": "uuid",
    "eventEntityId": "uuid",
    "dateId": "uuid",
    "sold": "integer",
    "capacity": "integer",
    "status": "EXHAUSTED"
  }
}
```

### BatchExpired

| Field | Value |
|---|---|
| **Producer** | Seating/Inventory |
| **Consumers** | Reporting |
| **Trigger** | Batch reaches its end time without being exhausted. |

```json
{
  "eventType": "BatchExpired",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "batchId": "uuid",
    "eventEntityId": "uuid",
    "dateId": "uuid",
    "sold": "integer",
    "capacity": "integer",
    "status": "EXPIRED"
  }
}
```

### PromotionCreated

| Field | Value |
|---|---|
| **Producer** | Orders |
| **Consumers** | Reporting |
| **Trigger** | Organizer or Marketing creates a Promotion. |

```json
{
  "eventType": "PromotionCreated",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "promotionId": "uuid",
    "eventEntityId": "uuid",
    "code": "string",
    "discountType": "PERCENTAGE | FIXED",
    "discountValue": "decimal",
    "maxUses": "integer",
    "usedCount": 0,
    "status": "CREATED"
  }
}
```

### PromotionActivated

| Field | Value |
|---|---|
| **Producer** | Orders |
| **Consumers** | Reporting |
| **Trigger** | Promotion becomes active (manually or by schedule). |

```json
{
  "eventType": "PromotionActivated",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "promotionId": "uuid",
    "status": "ACTIVE"
  }
}
```

### PromotionDeactivated

| Field | Value |
|---|---|
| **Producer** | Orders |
| **Consumers** | Reporting |
| **Trigger** | Organizer or Marketing manually deactivates a Promotion. |

```json
{
  "eventType": "PromotionDeactivated",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "promotionId": "uuid",
    "status": "DEACTIVATED"
  }
}
```

### PromotionExhausted

| Field | Value |
|---|---|
| **Producer** | Orders |
| **Consumers** | Reporting |
| **Trigger** | Promotion.usedCount reaches Promotion.maxUses. |

```json
{
  "eventType": "PromotionExhausted",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "promotionId": "uuid",
    "usedCount": "integer",
    "maxUses": "integer",
    "status": "EXHAUSTED"
  }
}
```

---

## Purchase Flow Events

### ReservationCreated

| Field | Value |
|---|---|
| **Producer** | Seating/Inventory |
| **Consumers** | Orders, Notifications |
| **Trigger** | Buyer selects a Seat and the system atomically creates a Reservation. |

```json
{
  "eventType": "ReservationCreated",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "reservationId": "uuid",
    "seatId": "uuid",
    "buyerId": "uuid",
    "eventEntityId": "uuid",
    "dateId": "uuid",
    "batchId": "uuid",
    "expiresAt": "ISO-8601",
    "status": "ACTIVE"
  }
}
```

### ReservationExpired

| Field | Value |
|---|---|
| **Producer** | Seating/Inventory |
| **Consumers** | Orders, Notifications |
| **Trigger** | Expiration job detects Reservation.expiresAt ≤ now without completed payment. |

```json
{
  "eventType": "ReservationExpired",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "reservationId": "uuid",
    "seatId": "uuid",
    "buyerId": "uuid",
    "eventEntityId": "uuid",
    "dateId": "uuid",
    "batchId": "uuid",
    "status": "EXPIRED"
  }
}
```

### ReservationReleased

| Field | Value |
|---|---|
| **Producer** | Seating/Inventory |
| **Consumers** | Orders, Notifications |
| **Trigger** | PaymentFailed or Platform Operator override releases the Reservation. |

```json
{
  "eventType": "ReservationReleased",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "reservationId": "uuid",
    "seatId": "uuid",
    "buyerId": "uuid",
    "eventEntityId": "uuid",
    "dateId": "uuid",
    "releasedBy": "string",
    "status": "RELEASED"
  }
}
```

### OrderCreated

| Field | Value |
|---|---|
| **Producer** | Orders |
| **Consumers** | Payments, Notifications |
| **Trigger** | Buyer proceeds to checkout after Reservation is created. |

```json
{
  "eventType": "OrderCreated",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "orderId": "uuid",
    "buyerId": "uuid",
    "eventEntityId": "uuid",
    "dateId": "uuid",
    "reservationId": "uuid",
    "lineItems": [
      {
        "lineItemId": "uuid",
        "seatId": "uuid",
        "batchPrice": "decimal",
        "quantity": "integer"
      }
    ],
    "subtotal": "decimal",
    "promotionId": "uuid | null",
    "promotionDiscount": "decimal",
    "serviceFee": "decimal",
    "total": "decimal",
    "currency": "string",
    "status": "CREATED"
  }
}
```

### OrderExpired

| Field | Value |
|---|---|
| **Producer** | Orders |
| **Consumers** | Notifications |
| **Trigger** | Associated Reservation expires (ReservationExpired received). |

```json
{
  "eventType": "OrderExpired",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "orderId": "uuid",
    "buyerId": "uuid",
    "reservationId": "uuid",
    "status": "EXPIRED"
  }
}
```

### PaymentInitiated

| Field | Value |
|---|---|
| **Producer** | Payments |
| **Consumers** | — |
| **Trigger** | Buyer submits payment credentials and Payment record is created. |

```json
{
  "eventType": "PaymentInitiated",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "paymentId": "uuid",
    "orderId": "uuid",
    "buyerId": "uuid",
    "amount": "decimal",
    "serviceFee": "decimal",
    "currency": "string",
    "method": "CARD | TRANSFER",
    "idempotencyKey": "string",
    "status": "INITIATED"
  }
}
```

### PaymentAuthorized

| Field | Value |
|---|---|
| **Producer** | Payments |
| **Consumers** | Orders, Ticket Issuance |
| **Trigger** | Payment gateway authorizes the transaction. |

```json
{
  "eventType": "PaymentAuthorized",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "paymentId": "uuid",
    "orderId": "uuid",
    "buyerId": "uuid",
    "amount": "decimal",
    "serviceFee": "decimal",
    "currency": "string",
    "method": "CARD | TRANSFER",
    "gatewayReference": "string",
    "status": "AUTHORIZED"
  }
}
```

### PaymentFailed

| Field | Value |
|---|---|
| **Producer** | Payments |
| **Consumers** | Orders, Notifications, Seating/Inventory |
| **Trigger** | Payment gateway rejects or fails the transaction. |

```json
{
  "eventType": "PaymentFailed",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "paymentId": "uuid",
    "orderId": "uuid",
    "buyerId": "uuid",
    "amount": "decimal",
    "currency": "string",
    "method": "CARD | TRANSFER",
    "failureReason": "string",
    "status": "FAILED"
  }
}
```

### TicketIssued

| Field | Value |
|---|---|
| **Producer** | Ticket Issuance |
| **Consumers** | Notifications, Reporting |
| **Trigger** | PaymentAuthorized consumed; Ticket created with QR. |

```json
{
  "eventType": "TicketIssued",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "ticketId": "uuid",
    "orderId": "uuid",
    "buyerId": "uuid",
    "eventEntityId": "uuid",
    "dateId": "uuid",
    "seatId": "uuid | null",
    "type": "MAPPED | GENERAL_ADMISSION",
    "holderName": "string",
    "accessPolicy": "string",
    "deliveryChannels": ["EMAIL", "PDF", "QR", "WALLET", "DOWNLOAD"],
    "status": "ISSUED",
    "issuedAt": "ISO-8601"
  }
}
```

### TicketDelivered

| Field | Value |
|---|---|
| **Producer** | Ticket Issuance |
| **Consumers** | Notifications |
| **Trigger** | All delivery channels for the Ticket have successfully delivered. |

```json
{
  "eventType": "TicketDelivered",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "ticketId": "uuid",
    "buyerId": "uuid",
    "deliveryChannels": ["EMAIL", "PDF"],
    "deliveredAt": "ISO-8601"
  }
}
```

---

## Post-Issuance Events

### TicketCanceled

| Field | Value |
|---|---|
| **Producer** | Ticket Issuance |
| **Consumers** | Access Control, Reporting, Notifications |
| **Trigger** | Platform Operator, Organizer, or Super Admin cancels a Ticket. |

```json
{
  "eventType": "TicketCanceled",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "ticketId": "uuid",
    "orderId": "uuid",
    "buyerId": "uuid",
    "seatId": "uuid | null",
    "canceledBy": "uuid",
    "status": "CANCELED"
  }
}
```

### TicketInvalidated

| Field | Value |
|---|---|
| **Producer** | Ticket Issuance |
| **Consumers** | Access Control, Reporting |
| **Trigger** | Fraud detected by Access Control (ConflictDetected); Ticket Issuance reacts by invalidating. |

```json
{
  "eventType": "TicketInvalidated",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "ticketId": "uuid",
    "orderId": "uuid",
    "buyerId": "uuid",
    "seatId": "uuid | null",
    "reason": "CONFLICT_DETECTED | MANUAL",
    "status": "INVALIDATED"
  }
}
```

---

## Access Control Events

### ValidationAttempted

| Field | Value |
|---|---|
| **Producer** | Access Control |
| **Consumers** | Reporting |
| **Trigger** | Door Validator scans a QR code. |

```json
{
  "eventType": "ValidationAttempted",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "validationId": "uuid",
    "ticketId": "uuid",
    "validatorDeviceId": "string",
    "eventEntityId": "uuid",
    "dateId": "uuid",
    "attemptedAt": "ISO-8601",
    "isOffline": "boolean"
  }
}
```

### ValidationSucceeded

| Field | Value |
|---|---|
| **Producer** | Access Control |
| **Consumers** | Ticket Issuance, Reporting |
| **Trigger** | QR scan passes all checks (Ticket ISSUED, QR not expired, Event matches, accessPolicy allows). |

```json
{
  "eventType": "ValidationSucceeded",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "validationId": "uuid",
    "ticketId": "uuid",
    "validatorDeviceId": "string",
    "eventEntityId": "uuid",
    "dateId": "uuid",
    "result": "SUCCEEDED",
    "isOffline": "boolean",
    "syncedAt": "ISO-8601 | null"
  }
}
```

### ValidationFailed

| Field | Value |
|---|---|
| **Producer** | Access Control |
| **Consumers** | Reporting |
| **Trigger** | QR scan fails one or more checks. |

```json
{
  "eventType": "ValidationFailed",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "validationId": "uuid",
    "ticketId": "uuid",
    "validatorDeviceId": "string",
    "eventEntityId": "uuid",
    "dateId": "uuid",
    "result": "FAILED",
    "failureReason": "ALREADY_USED | WRONG_EVENT | EXPIRED | INVALIDATED",
    "isOffline": "boolean",
    "syncedAt": "ISO-8601 | null"
  }
}
```

### ValidatorSyncRequested

| Field | Value |
|---|---|
| **Producer** | Access Control |
| **Consumers** | — (internal) |
| **Trigger** | Offline Validator device reconnects and initiates sync. |

```json
{
  "eventType": "ValidatorSyncRequested",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "validatorDeviceId": "string",
    "eventEntityId": "uuid",
    "dateId": "uuid",
    "queuedRecords": "integer"
  }
}
```

### ValidatorSynced

| Field | Value |
|---|---|
| **Producer** | Access Control |
| **Consumers** | Reporting |
| **Trigger** | All queued offline ValidationRecords have been processed and reconciled. |

```json
{
  "eventType": "ValidatorSynced",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "validatorDeviceId": "string",
    "eventEntityId": "uuid",
    "dateId": "uuid",
    "totalSynced": "integer",
    "conflictsDetected": "integer"
  }
}
```

### ConflictDetected

| Field | Value |
|---|---|
| **Producer** | Access Control |
| **Consumers** | Ticket Issuance, Reporting |
| **Trigger** | During offline sync, a Ticket was already marked as USED by another scan (first-scan-wins rule). |

```json
{
  "eventType": "ConflictDetected",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "validationId": "uuid",
    "ticketId": "uuid",
    "validatorDeviceId": "string",
    "conflictWithValidationId": "uuid",
    "eventEntityId": "uuid",
    "dateId": "uuid"
  }
}
```

---

## Notification Events

### NotificationDispatched

| Field | Value |
|---|---|
| **Producer** | Notifications |
| **Consumers** | — (terminal) |
| **Trigger** | Notification sent to external provider. |

```json
{
  "eventType": "NotificationDispatched",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "notificationId": "uuid",
    "recipientId": "uuid",
    "channel": "EMAIL | SMS | WHATSAPP",
    "templateId": "string",
    "triggeredBy": "string",
    "status": "DISPATCHED"
  }
}
```

### NotificationDelivered

| Field | Value |
|---|---|
| **Producer** | Notifications |
| **Consumers** | — (terminal) |
| **Trigger** | External provider confirms delivery. |

```json
{
  "eventType": "NotificationDelivered",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "notificationId": "uuid",
    "recipientId": "uuid",
    "channel": "EMAIL | SMS | WHATSAPP",
    "status": "DELIVERED"
  }
}
```

### NotificationFailed

| Field | Value |
|---|---|
| **Producer** | Notifications |
| **Consumers** | — (terminal) |
| **Trigger** | External provider returns delivery failure. |

```json
{
  "eventType": "NotificationFailed",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "notificationId": "uuid",
    "recipientId": "uuid",
    "channel": "EMAIL | SMS | WHATSAPP",
    "retryCount": "integer",
    "failureReason": "string",
    "status": "FAILED"
  }
}
```

---

## Financial Events

### ServiceFeeCalculated

| Field | Value |
|---|---|
| **Producer** | Payments |
| **Consumers** | Reporting |
| **Trigger** | PaymentAuthorized processed; platform's Service Fee amount determined. |

```json
{
  "eventType": "ServiceFeeCalculated",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "paymentId": "uuid",
    "orderId": "uuid",
    "organizerId": "uuid",
    "eventEntityId": "uuid",
    "dateId": "uuid",
    "grossAmount": "decimal",
    "serviceFee": "decimal",
    "netAmount": "decimal",
    "currency": "string"
  }
}
```

### PayoutGenerated

| Field | Value |
|---|---|
| **Producer** | Payments |
| **Consumers** | Reporting, Notifications |
| **Trigger** | Event Date passes; system aggregates confirmed payments and generates Payout for Organizer. |

```json
{
  "eventType": "PayoutGenerated",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "payoutId": "uuid",
    "organizerId": "uuid",
    "eventEntityId": "uuid",
    "dateId": "uuid",
    "grossAmount": "decimal",
    "serviceFeeTotal": "decimal",
    "netAmount": "decimal",
    "currency": "string",
    "status": "PENDING"
  }
}
```

### PayoutProcessed

| Field | Value |
|---|---|
| **Producer** | Payments |
| **Consumers** | Reporting, Notifications |
| **Trigger** | Settlement to Organizer bank account succeeds. |

```json
{
  "eventType": "PayoutProcessed",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "payoutId": "uuid",
    "organizerId": "uuid",
    "netAmount": "decimal",
    "currency": "string",
    "processedAt": "ISO-8601",
    "status": "PROCESSED"
  }
}
```

### PayoutFailed

| Field | Value |
|---|---|
| **Producer** | Payments |
| **Consumers** | Reporting, Notifications |
| **Trigger** | Settlement to Organizer fails (after max 1 automatic retry). |

```json
{
  "eventType": "PayoutFailed",
  "eventId": "uuid",
  "occurredAt": "ISO-8601",
  "payload": {
    "payoutId": "uuid",
    "organizerId": "uuid",
    "netAmount": "decimal",
    "currency": "string",
    "retryCount": "integer",
    "failureReason": "string",
    "status": "FAILED"
  }
}
```
