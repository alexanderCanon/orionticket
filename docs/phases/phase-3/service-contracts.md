# Service Contracts

> **Phase:** 3 — Architecture  
> **Source:** `docs/phases/phase-0/discovery.md` Block 11.6, `docs/phases/phase-2/critical-flows.md`, `docs/phases/phase-2/use-case-catalog.md`  
> **Scope:** REST API contract for every synchronous interaction. All paths prefixed `/v1/`.  
> **Format:** Owner service, method, path, parameters, request body, response schema, error codes, related use case.

---

## Identity Service

### POST /v1/auth/register

| Field | Value |
|---|---|
| **Owner** | Identity |
| **Related use case** | UC-ID-01 |
| **Description** | Buyer self-registration. |

**Request body:**
```json
{
  "email": "string (required)",
  "password": "string (required, min 8 chars)",
  "fullName": "string (required)",
  "phone": "string (required)"
}
```

**Response (201 Created):**
```json
{
  "userId": "uuid",
  "email": "string",
  "fullName": "string",
  "status": "UNVERIFIED"
}
```

**Error codes:**
| Code | Meaning |
|---|---|
| 400 | Invalid request format. |
| 409 | Email already exists. |

---

## Event Management Service

### POST /v1/events

| Field | Value |
|---|---|
| **Owner** | Event Management |
| **Related use case** | UC-EM-01 |
| **Description** | Organizer creates a new Event. |

**Request body:**
```json
{
  "name": "string (required)",
  "description": "string (optional)"
}
```

**Response (201 Created):**
```json
{
  "eventId": "uuid",
  "organizerId": "uuid",
  "name": "string",
  "description": "string",
  "status": "DRAFT",
  "dates": [],
  "createdAt": "datetime"
}
```

### POST /v1/events/{eventId}/dates

| Field | Value |
|---|---|
| **Owner** | Event Management |
| **Related use case** | UC-EM-01 |
| **Description** | Add a date to an existing DRAFT event. |

**Request body:**
```json
{
  "scheduledAt": "datetime (required)",
  "venueId": "uuid (required)",
  "capacity": "integer (required, >0)"
}
```

**Response (201 Created):**
```json
{
  "dateId": "uuid",
  "eventId": "uuid",
  "scheduledAt": "datetime",
  "venueId": "uuid",
  "capacity": "integer"
}
```

### POST /v1/events/{eventId}/submit

| Field | Value |
|---|---|
| **Owner** | Event Management |
| **Related use case** | UC-EM-03 |
| **Description** | Submit event for review. |

**Response (200 OK):**
```json
{
  "eventId": "uuid",
  "status": "UNDER_REVIEW"
}
```

### POST /v1/events/{eventId}/approve

| Field | Value |
|---|---|
| **Owner** | Event Management |
| **Related use case** | UC-EM-04 |
| **Description** | Platform Operator approves an event under review. |

**Response (200 OK):**
```json
{
  "eventId": "uuid",
  "status": "RELEASED"
}
```

### POST /v1/events/{eventId}/reject

| Field | Value |
|---|---|
| **Owner** | Event Management |
| **Related use case** | UC-EM-05 |
| **Description** | Platform Operator rejects an event under review. |

**Request body:**
```json
{
  "reason": "string (required)"
}
```

**Response (200 OK):**
```json
{
  "eventId": "uuid",
  "status": "DRAFT"
}
```

### POST /v1/venues

| Field | Value |
|---|---|
| **Owner** | Event Management |
| **Related use case** | UC-EM-02 |
| **Description** | Organizer creates a Venue. |

**Request body:**
```json
{
  "name": "string (required)",
  "address": "string (required)",
  "capacity": "integer (required, >0)"
}
```

**Response (201 Created):**
```json
{
  "venueId": "uuid",
  "organizerId": "uuid",
  "name": "string",
  "address": "string",
  "capacity": "integer"
}
```

### GET /v1/venues

| Field | Value |
|---|---|
| **Owner** | Event Management |
| **Related use case** | UC-EM-02 |
| **Description** | Get venues for the authenticated organizer. |

**Response (200 OK):**
```json
[
  {
    "venueId": "uuid",
    "name": "string",
    "address": "string",
    "capacity": "integer"
  }
]
```
| 409 | Email already registered |
| 422 | Validation error (missing/invalid fields) |

---

### POST /v1/auth/login

| Field | Value |
|---|---|
| **Owner** | Identity |
| **Related use case** | UC-ID-01 (prerequisite for all authenticated flows) |
| **Description** | Authenticate user and return session token. |

**Request body:**
```json
{
  "email": "string (required)",
  "password": "string (required)"
}
```

**Response (200 OK):**
```json
{
  "accessToken": "string (JWT)",
  "tokenType": "Bearer",
  "expiresIn": "integer (seconds)",
  "userId": "uuid",
  "role": "string",
  "organizerId": "uuid | null"
}
```

**Error codes:**
| Code | Meaning |
|---|---|
| 401 | Invalid credentials |
| 403 | User SUSPENDED or UNVERIFIED |

---

### GET /v1/users/{userId}

| Field | Value |
|---|---|
| **Owner** | Identity |
| **Related use case** | UC-ID-03 (inter-service lookup) |
| **Description** | Retrieve user details by ID. Used by other services for synchronous user lookup. |

**Path parameters:** `userId` (uuid)

**Response (200 OK):**
```json
{
  "userId": "uuid",
  "email": "string",
  "fullName": "string",
  "phone": "string",
  "status": "ACTIVE | SUSPENDED | UNVERIFIED",
  "roleId": "uuid",
  "organizerId": "uuid | null"
}
```

**Error codes:**
| Code | Meaning |
|---|---|
| 404 | User not found |

---

### POST /v1/organizers/{organizerId}/staff

| Field | Value |
|---|---|
| **Owner** | Identity |
| **Related use case** | UC-ID-02 |
| **Description** | Organizer creates a staff User (Venue Staff or Door Validator). |

**Path parameters:** `organizerId` (uuid)

**Request body:**
```json
{
  "email": "string (required)",
  "fullName": "string (required)",
  "phone": "string (required)",
  "role": "VENUE_STAFF | DOOR_VALIDATOR (required)"
}
```

**Response (201 Created):**
```json
{
  "userId": "uuid",
  "email": "string",
  "role": "string",
  "organizerId": "uuid",
  "status": "UNVERIFIED"
}
```

**Error codes:**
| Code | Meaning |
|---|---|
| 403 | Caller is not the Organizer for this organizerId |
| 409 | Email already registered |
| 422 | Invalid role (only VENUE_STAFF, DOOR_VALIDATOR allowed) |

---

## Event Management Service

### POST /v1/events

| Field | Value |
|---|---|
| **Owner** | Event Management |
| **Related use case** | UC-EM-01 |
| **Description** | Organizer creates a new Event in DRAFT status. |

**Request body:**
```json
{
  "organizerId": "uuid (required)",
  "name": "string (required)",
  "description": "string",
  "category": "string (required)"
}
```

**Response (201 Created):**
```json
{
  "eventId": "uuid",
  "organizerId": "uuid",
  "name": "string",
  "category": "string",
  "status": "DRAFT",
  "createdAt": "ISO-8601"
}
```

**Error codes:**
| Code | Meaning |
|---|---|
| 403 | Caller is not an Organizer |
| 422 | Validation error |

---

### POST /v1/events/{eventId}/dates

| Field | Value |
|---|---|
| **Owner** | Event Management |
| **Related use case** | UC-EM-01 |
| **Description** | Add a Date (performance) to an Event. |

**Path parameters:** `eventId` (uuid)

**Request body:**
```json
{
  "scheduledAt": "ISO-8601 (required)",
  "venueId": "uuid (required)",
  "capacity": "integer (required)"
}
```

**Response (201 Created):**
```json
{
  "dateId": "uuid",
  "eventId": "uuid",
  "scheduledAt": "ISO-8601",
  "venueId": "uuid",
  "capacity": "integer"
}
```

**Error codes:**
| Code | Meaning |
|---|---|
| 404 | Event not found |
| 403 | Event does not belong to caller's Organizer |
| 422 | Validation error |

---

### POST /v1/events/{eventId}/submit

| Field | Value |
|---|---|
| **Owner** | Event Management |
| **Related use case** | UC-EM-03 |
| **Description** | Organizer submits Event for Platform Operator review. |

**Path parameters:** `eventId` (uuid)

**Request body:** None

**Response (200 OK):**
```json
{
  "eventId": "uuid",
  "status": "UNDER_REVIEW"
}
```

**Error codes:**
| Code | Meaning |
|---|---|
| 404 | Event not found |
| 403 | Not the owning Organizer |
| 409 | Event not in DRAFT status |
| 422 | Missing required data (no Dates, no Batch, no Seating Map) |

---

### POST /v1/events/{eventId}/approve

| Field | Value |
|---|---|
| **Owner** | Event Management |
| **Related use case** | UC-EM-04 |
| **Description** | Platform Operator approves Event. Sets status to RELEASED. |

**Path parameters:** `eventId` (uuid)

**Request body:** None

**Response (200 OK):**
```json
{
  "eventId": "uuid",
  "status": "RELEASED"
}
```

**Error codes:**
| Code | Meaning |
|---|---|
| 404 | Event not found |
| 403 | Caller is not Platform Operator or Super Admin |
| 409 | Event not in UNDER_REVIEW status |

---

### POST /v1/events/{eventId}/reject

| Field | Value |
|---|---|
| **Owner** | Event Management |
| **Related use case** | UC-EM-04 |
| **Description** | Platform Operator rejects Event. Returns to DRAFT with reason. |

**Path parameters:** `eventId` (uuid)

**Request body:**
```json
{
  "reason": "string (required)"
}
```

**Response (200 OK):**
```json
{
  "eventId": "uuid",
  "status": "DRAFT",
  "rejectionReason": "string"
}
```

**Error codes:**
| Code | Meaning |
|---|---|
| 404 | Event not found |
| 403 | Caller is not Platform Operator or Super Admin |
| 409 | Event not in UNDER_REVIEW status |

---

### POST /v1/events/{eventId}/cancel

| Field | Value |
|---|---|
| **Owner** | Event Management |
| **Related use case** | UC-EM-05 |
| **Description** | Cancel an Event. Fires EventCanceled and DateCanceled for all Dates. |

**Path parameters:** `eventId` (uuid)

**Request body:**
```json
{
  "reason": "string (required)"
}
```

**Response (200 OK):**
```json
{
  "eventId": "uuid",
  "status": "CANCELED"
}
```

**Error codes:**
| Code | Meaning |
|---|---|
| 404 | Event not found |
| 403 | Caller is not the Organizer, Platform Operator, or Super Admin |
| 409 | Event already canceled |

---

### GET /v1/catalog/events

| Field | Value |
|---|---|
| **Owner** | Event Management (Catalog read model) |
| **Related use case** | UC-SI-02 (discovery step) |
| **Description** | Public endpoint. Returns released Events with Dates and availability. |

**Query parameters:** `category` (string, optional), `city` (string, optional), `date` (ISO-8601, optional), `organizerId` (uuid, optional), `page` (integer), `size` (integer)

**Response (200 OK):**
```json
{
  "events": [
    {
      "eventId": "uuid",
      "name": "string",
      "category": "string",
      "organizerName": "string",
      "dates": [
        {
          "dateId": "uuid",
          "scheduledAt": "ISO-8601",
          "venueName": "string",
          "availableSeats": "integer"
        }
      ]
    }
  ],
  "page": "integer",
  "totalPages": "integer"
}
```

**Error codes:**
| Code | Meaning |
|---|---|
| 422 | Invalid filter parameters |

---

### GET /v1/events/{eventId}

| Field | Value |
|---|---|
| **Owner** | Event Management |
| **Related use case** | UC-EM-01 (inter-service lookup) |
| **Description** | Retrieve Event details by ID. Used by other services for synchronous Event lookup. |

**Path parameters:** `eventId` (uuid)

**Response (200 OK):**
```json
{
  "eventId": "uuid",
  "organizerId": "uuid",
  "name": "string",
  "description": "string",
  "category": "string",
  "status": "DRAFT | UNDER_REVIEW | RELEASED | CANCELED",
  "dates": [
    {
      "dateId": "uuid",
      "scheduledAt": "ISO-8601",
      "venueId": "uuid",
      "capacity": "integer"
    }
  ]
}
```

**Error codes:**
| Code | Meaning |
|---|---|
| 404 | Event not found |

---

## Seating / Inventory Service

### POST /v1/events/{eventId}/dates/{dateId}/seating-map

| Field | Value |
|---|---|
| **Owner** | Seating/Inventory |
| **Related use case** | UC-EM-02 |
| **Description** | Configure Seating Map for a Date. Creates Seat records. |

**Path parameters:** `eventId` (uuid), `dateId` (uuid)

**Request body:**
```json
{
  "zones": [
    {
      "zone": "string",
      "sections": [
        {
          "section": "string",
          "rows": [
            {
              "row": "string",
              "seatNumbers": ["string"]
            }
          ]
        }
      ]
    }
  ],
  "generalAdmission": {
    "capacity": "integer"
  }
}
```

**Response (201 Created):**
```json
{
  "eventId": "uuid",
  "dateId": "uuid",
  "totalMappedSeats": "integer",
  "generalAdmissionCapacity": "integer"
}
```

**Error codes:**
| Code | Meaning |
|---|---|
| 404 | Event or Date not found |
| 403 | Not the owning Organizer |
| 409 | Event already RELEASED (Seating Map locked) |

---

### POST /v1/events/{eventId}/dates/{dateId}/batches

| Field | Value |
|---|---|
| **Owner** | Seating/Inventory |
| **Related use case** | UC-SI-01 |
| **Description** | Create a Batch for a Date. |

**Path parameters:** `eventId` (uuid), `dateId` (uuid)

**Request body:**
```json
{
  "name": "string (required)",
  "price": "decimal (required)",
  "currency": "string (required, default: GTQ)",
  "capacity": "integer (required)",
  "scheduledStartAt": "ISO-8601 (required)"
}
```

**Response (201 Created):**
```json
{
  "batchId": "uuid",
  "eventId": "uuid",
  "dateId": "uuid",
  "name": "string",
  "price": "decimal",
  "currency": "string",
  "capacity": "integer",
  "sold": 0,
  "status": "SCHEDULED",
  "scheduledStartAt": "ISO-8601"
}
```

**Error codes:**
| Code | Meaning |
|---|---|
| 404 | Event or Date not found |
| 403 | Not the owning Organizer |
| 422 | Validation error |

---

### GET /v1/events/{eventId}/dates/{dateId}/seats

| Field | Value |
|---|---|
| **Owner** | Seating/Inventory |
| **Related use case** | UC-SI-02 (seat selection view) |
| **Description** | Returns Seat availability for a Date. Public endpoint for Buyers. |

**Path parameters:** `eventId` (uuid), `dateId` (uuid)

**Query parameters:** `zone` (string, optional), `section` (string, optional)

**Response (200 OK):**
```json
{
  "eventId": "uuid",
  "dateId": "uuid",
  "seats": [
    {
      "seatId": "uuid",
      "zone": "string | null",
      "section": "string | null",
      "row": "string | null",
      "type": "MAPPED | GENERAL_ADMISSION",
      "status": "AVAILABLE | RESERVED | SOLD | BLOCKED",
      "batchId": "uuid",
      "batchName": "string",
      "price": "decimal",
      "currency": "string"
    }
  ]
}
```

**Error codes:**
| Code | Meaning |
|---|---|
| 404 | Event or Date not found |

---

### POST /v1/reservations

| Field | Value |
|---|---|
| **Owner** | Seating/Inventory |
| **Related use case** | UC-SI-02 |
| **Description** | **Synchronous, strong consistency.** Create a Reservation by holding a Seat. Atomically increments Batch.sold. This is a critical concurrency path. |

**Request body:**
```json
{
  "seatId": "uuid (required for MAPPED)",
  "eventId": "uuid (required)",
  "dateId": "uuid (required)",
  "batchId": "uuid (required)",
  "buyerId": "uuid (required)",
  "quantity": "integer (required, default: 1, only > 1 for GENERAL_ADMISSION)"
}
```

**Response (201 Created):**
```json
{
  "reservationId": "uuid",
  "seatId": "uuid",
  "buyerId": "uuid",
  "eventId": "uuid",
  "dateId": "uuid",
  "batchId": "uuid",
  "expiresAt": "ISO-8601",
  "status": "ACTIVE"
}
```

**Error codes:**
| Code | Meaning |
|---|---|
| 409 | Seat already RESERVED or SOLD (concurrency conflict) |
| 410 | Batch EXHAUSTED or EXPIRED |
| 404 | Seat, Event, Date, or Batch not found |
| 422 | Validation error |

---

### DELETE /v1/reservations/{reservationId}

| Field | Value |
|---|---|
| **Owner** | Seating/Inventory |
| **Related use case** | UC-OR-02 |
| **Description** | Platform Operator overrides (releases) a Reservation. |

**Path parameters:** `reservationId` (uuid)

**Response (200 OK):**
```json
{
  "reservationId": "uuid",
  "status": "RELEASED"
}
```

**Error codes:**
| Code | Meaning |
|---|---|
| 404 | Reservation not found |
| 403 | Caller is not Platform Operator or Super Admin |
| 409 | Reservation already EXPIRED or RELEASED |

---

## Orders Service

### POST /v1/orders

| Field | Value |
|---|---|
| **Owner** | Orders |
| **Related use case** | UC-OR-01 |
| **Description** | Create an Order from an active Reservation. Resolves price, applies Promotion, calculates Service Fee. |

**Request body:**
```json
{
  "buyerId": "uuid (required)",
  "eventId": "uuid (required)",
  "dateId": "uuid (required)",
  "reservationId": "uuid (required)",
  "promotionCode": "string (optional)"
}
```

**Response (201 Created):**
```json
{
  "orderId": "uuid",
  "buyerId": "uuid",
  "eventId": "uuid",
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
  "status": "CREATED",
  "createdAt": "ISO-8601"
}
```

**Error codes:**
| Code | Meaning |
|---|---|
| 404 | Reservation not found or expired |
| 409 | Order already exists for this Reservation (idempotency) |
| 422 | Invalid promotion code or promotion exhausted |

---

### GET /v1/orders/{orderId}

| Field | Value |
|---|---|
| **Owner** | Orders |
| **Related use case** | UC-OR-01 (inter-service lookup), Support order view |
| **Description** | Retrieve Order details by ID. |

**Path parameters:** `orderId` (uuid)

**Response (200 OK):**
```json
{
  "orderId": "uuid",
  "buyerId": "uuid",
  "eventId": "uuid",
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
  "status": "CREATED | PAYMENT_INITIATED | CONFIRMED | EXPIRED | FAILED",
  "createdAt": "ISO-8601"
}
```

**Error codes:**
| Code | Meaning |
|---|---|
| 404 | Order not found |
| 403 | Caller does not have access to this Order |

---

### GET /v1/buyers/{buyerId}/orders

| Field | Value |
|---|---|
| **Owner** | Orders |
| **Related use case** | UC-OR-01 (Buyer portal) |
| **Description** | List Orders for a specific Buyer. |

**Path parameters:** `buyerId` (uuid)

**Query parameters:** `status` (string, optional), `page` (integer), `size` (integer)

**Response (200 OK):**
```json
{
  "orders": [
    {
      "orderId": "uuid",
      "eventId": "uuid",
      "total": "decimal",
      "currency": "string",
      "status": "string",
      "createdAt": "ISO-8601"
    }
  ],
  "page": "integer",
  "totalPages": "integer"
}
```

**Error codes:**
| Code | Meaning |
|---|---|
| 403 | Caller is not the Buyer or authorized role (Support, Super Admin) |

---

## Payments Service

### POST /v1/payments

| Field | Value |
|---|---|
| **Owner** | Payments |
| **Related use case** | UC-PA-01 |
| **Description** | Initiate payment for an Order. Creates Payment record and calls gateway. |

**Request body:**
```json
{
  "orderId": "uuid (required)",
  "buyerId": "uuid (required)",
  "method": "CARD | TRANSFER (required)",
  "paymentDetails": {
    "gatewayToken": "string (required — tokenized card or transfer reference)"
  }
}
```

**Response (201 Created):**
```json
{
  "paymentId": "uuid",
  "orderId": "uuid",
  "amount": "decimal",
  "serviceFee": "decimal",
  "currency": "string",
  "method": "CARD | TRANSFER",
  "idempotencyKey": "string",
  "status": "INITIATED",
  "createdAt": "ISO-8601"
}
```

**Error codes:**
| Code | Meaning |
|---|---|
| 404 | Order not found |
| 409 | Order already paid (idempotency) or Order EXPIRED |
| 422 | Invalid payment method or gateway token |
| 502 | Payment gateway unavailable |

---

### POST /v1/payments/webhook

| Field | Value |
|---|---|
| **Owner** | Payments |
| **Related use case** | UC-PA-01, UC-PA-02 |
| **Description** | **Synchronous call from payment gateway webhook.** Receives authorization or failure result. Updates Payment and fires PaymentAuthorized or PaymentFailed. Then updates Order status synchronously. |

**Request body:** (gateway-specific; abstracted here)
```json
{
  "gatewayReference": "string",
  "paymentId": "uuid",
  "result": "AUTHORIZED | FAILED",
  "failureReason": "string | null"
}
```

**Response (200 OK):**
```json
{
  "acknowledged": true
}
```

**Error codes:**
| Code | Meaning |
|---|---|
| 400 | Invalid webhook payload |
| 404 | Payment not found |

---

### GET /v1/payments/{paymentId}

| Field | Value |
|---|---|
| **Owner** | Payments |
| **Related use case** | UC-PA-01 (status check) |
| **Description** | Retrieve Payment status. |

**Path parameters:** `paymentId` (uuid)

**Response (200 OK):**
```json
{
  "paymentId": "uuid",
  "orderId": "uuid",
  "buyerId": "uuid",
  "amount": "decimal",
  "serviceFee": "decimal",
  "currency": "string",
  "method": "CARD | TRANSFER",
  "gatewayReference": "string | null",
  "status": "INITIATED | AUTHORIZED | FAILED",
  "createdAt": "ISO-8601"
}
```

**Error codes:**
| Code | Meaning |
|---|---|
| 404 | Payment not found |

---

### GET /v1/payouts

| Field | Value |
|---|---|
| **Owner** | Payments |
| **Related use case** | UC-PA-03 (Finance / Organizer reads) |
| **Description** | List Payouts. Filtered by organizerId for Organizer role. |

**Query parameters:** `organizerId` (uuid, optional), `status` (string, optional), `page` (integer), `size` (integer)

**Response (200 OK):**
```json
{
  "payouts": [
    {
      "payoutId": "uuid",
      "organizerId": "uuid",
      "eventId": "uuid",
      "dateId": "uuid",
      "grossAmount": "decimal",
      "serviceFeeTotal": "decimal",
      "netAmount": "decimal",
      "status": "PENDING | PROCESSED | FAILED",
      "triggeredAt": "ISO-8601",
      "processedAt": "ISO-8601 | null"
    }
  ],
  "page": "integer",
  "totalPages": "integer"
}
```

**Error codes:**
| Code | Meaning |
|---|---|
| 403 | Organizer can only view own Payouts |

---

## Ticket Issuance Service

### GET /v1/tickets/{ticketId}

| Field | Value |
|---|---|
| **Owner** | Ticket Issuance |
| **Related use case** | UC-AC-01 (Access Control QR lookup — synchronous, 100 ms SLA) |
| **Description** | **Synchronous, strong consistency.** Retrieve Ticket details including current QR validity. Called by Access Control during door validation. |

**Path parameters:** `ticketId` (uuid)

**Response (200 OK):**
```json
{
  "ticketId": "uuid",
  "orderId": "uuid",
  "buyerId": "uuid",
  "eventId": "uuid",
  "dateId": "uuid",
  "seatId": "uuid | null",
  "type": "MAPPED | GENERAL_ADMISSION",
  "holderName": "string",
  "qrCode": "string (current)",
  "qrExpiresAt": "ISO-8601",
  "accessPolicy": "string",
  "status": "ISSUED | CANCELED | INVALIDATED | USED",
  "issuedAt": "ISO-8601"
}
```

**Error codes:**
| Code | Meaning |
|---|---|
| 404 | Ticket not found |

---

### GET /v1/buyers/{buyerId}/tickets

| Field | Value |
|---|---|
| **Owner** | Ticket Issuance |
| **Related use case** | UC-TI-01 (Buyer portal) |
| **Description** | List Buyer's Tickets. |

**Path parameters:** `buyerId` (uuid)

**Query parameters:** `eventId` (uuid, optional), `status` (string, optional), `page` (integer), `size` (integer)

**Response (200 OK):**
```json
{
  "tickets": [
    {
      "ticketId": "uuid",
      "eventId": "uuid",
      "dateId": "uuid",
      "seatId": "uuid | null",
      "type": "MAPPED | GENERAL_ADMISSION",
      "holderName": "string",
      "qrCode": "string",
      "qrExpiresAt": "ISO-8601",
      "status": "string",
      "issuedAt": "ISO-8601"
    }
  ],
  "page": "integer",
  "totalPages": "integer"
}
```

**Error codes:**
| Code | Meaning |
|---|---|
| 403 | Caller is not the Buyer or authorized role |

---

### POST /v1/tickets/{ticketId}/cancel

| Field | Value |
|---|---|
| **Owner** | Ticket Issuance |
| **Related use case** | UC-TI-02 |
| **Description** | Cancel a Ticket. |

**Path parameters:** `ticketId` (uuid)

**Request body:**
```json
{
  "canceledBy": "uuid (required)",
  "reason": "string (optional)"
}
```

**Response (200 OK):**
```json
{
  "ticketId": "uuid",
  "status": "CANCELED"
}
```

**Error codes:**
| Code | Meaning |
|---|---|
| 404 | Ticket not found |
| 403 | Caller is not Organizer, Platform Operator, or Super Admin |
| 409 | Ticket already USED, CANCELED, or INVALIDATED |

---

### POST /v1/tickets/{ticketId}/resend

| Field | Value |
|---|---|
| **Owner** | Ticket Issuance |
| **Related use case** | UC-TI-03 |
| **Description** | Support triggers manual resend of a Ticket. Creates a new Notification delivery. |

**Path parameters:** `ticketId` (uuid)

**Request body:** None

**Response (200 OK):**
```json
{
  "ticketId": "uuid",
  "notificationId": "uuid",
  "status": "DISPATCHED"
}
```

**Error codes:**
| Code | Meaning |
|---|---|
| 404 | Ticket not found |
| 403 | Caller is not Support or Super Admin |
| 409 | Ticket not in ISSUED status |

---

## Access Control Service

### POST /v1/validations

| Field | Value |
|---|---|
| **Owner** | Access Control |
| **Related use case** | UC-AC-01, UC-AC-02 |
| **Description** | **Synchronous, strong consistency.** QR scan at the door. Calls Ticket Issuance to verify, creates ValidationRecord, returns result. Must respond within 100 ms. |

**Request body:**
```json
{
  "ticketId": "uuid (required, decoded from QR)",
  "validatorDeviceId": "string (required)",
  "eventId": "uuid (required)",
  "dateId": "uuid (required)"
}
```

**Response (200 OK):**
```json
{
  "validationId": "uuid",
  "ticketId": "uuid",
  "result": "SUCCEEDED | FAILED",
  "failureReason": "null | ALREADY_USED | WRONG_EVENT | EXPIRED | INVALIDATED",
  "isOffline": false,
  "attemptedAt": "ISO-8601"
}
```

**Error codes:**
| Code | Meaning |
|---|---|
| 404 | Ticket not found (via Ticket Issuance lookup) |
| 422 | Missing/invalid fields |

---

### POST /v1/validations/sync

| Field | Value |
|---|---|
| **Owner** | Access Control |
| **Related use case** | UC-AC-03 |
| **Description** | Offline Validator device syncs queued scans after reconnection. |

**Request body:**
```json
{
  "validatorDeviceId": "string (required)",
  "eventId": "uuid (required)",
  "dateId": "uuid (required)",
  "records": [
    {
      "ticketId": "uuid",
      "attemptedAt": "ISO-8601"
    }
  ]
}
```

**Response (200 OK):**
```json
{
  "validatorDeviceId": "string",
  "totalSynced": "integer",
  "results": [
    {
      "validationId": "uuid",
      "ticketId": "uuid",
      "result": "SUCCEEDED | FAILED",
      "failureReason": "null | ALREADY_USED",
      "conflictDetected": "boolean",
      "syncedAt": "ISO-8601"
    }
  ],
  "conflictsDetected": "integer"
}
```

**Error codes:**
| Code | Meaning |
|---|---|
| 422 | Invalid or empty records array |

---

## Notifications Service

### GET /v1/notifications

| Field | Value |
|---|---|
| **Owner** | Notifications |
| **Related use case** | UC-NO-01, UC-TI-03 |
| **Description** | Retrieve Notification delivery records for operational support and audit visibility. Notifications is primarily event-driven; this endpoint exposes the delivery log, not a command to create business state. |

**Query parameters:** `recipientId` (uuid, optional), `status` (`PENDING | DISPATCHED | DELIVERED | FAILED`, optional), `channel` (`EMAIL | SMS | WHATSAPP`, optional), `triggeredBy` (string, optional), `page` (integer, optional), `size` (integer, optional)

**Response (200 OK):**
```json
{
  "notifications": [
    {
      "notificationId": "uuid",
      "recipientId": "uuid",
      "channel": "EMAIL | SMS | WHATSAPP",
      "templateId": "string",
      "status": "PENDING | DISPATCHED | DELIVERED | FAILED",
      "retryCount": "integer",
      "triggeredBy": "string",
      "createdAt": "ISO-8601"
    }
  ],
  "page": "integer",
  "totalPages": "integer"
}
```

**Error codes:**
| Code | Meaning |
|---|---|
| 403 | Caller is not Support, Platform Operator, or Super Admin |
| 422 | Invalid filter or pagination parameter |

---

### GET /v1/notifications/{notificationId}

| Field | Value |
|---|---|
| **Owner** | Notifications |
| **Related use case** | UC-NO-01 |
| **Description** | Retrieve a single Notification delivery record, including its payload, for support troubleshooting. |

**Path parameters:** `notificationId` (uuid)

**Response (200 OK):**
```json
{
  "notificationId": "uuid",
  "recipientId": "uuid",
  "channel": "EMAIL | SMS | WHATSAPP",
  "templateId": "string",
  "payload": {
    "key": "value"
  },
  "status": "PENDING | DISPATCHED | DELIVERED | FAILED",
  "retryCount": "integer",
  "triggeredBy": "string",
  "createdAt": "ISO-8601"
}
```

**Error codes:**
| Code | Meaning |
|---|---|
| 403 | Caller is not Support, Platform Operator, or Super Admin |
| 404 | Notification not found |

---

### POST /v1/notifications/{notificationId}/retry

| Field | Value |
|---|---|
| **Owner** | Notifications |
| **Related use case** | UC-NO-01, UC-TI-03 |
| **Description** | Retry delivery for a specific FAILED or PENDING Notification. This is an operational delivery retry; Ticket resend remains owned by Ticket Issuance through `POST /v1/tickets/{ticketId}/resend`. |

**Path parameters:** `notificationId` (uuid)

**Request body:** None

**Response (200 OK):**
```json
{
  "notificationId": "uuid",
  "recipientId": "uuid",
  "channel": "EMAIL | SMS | WHATSAPP",
  "status": "DISPATCHED | DELIVERED | FAILED",
  "retryCount": "integer"
}
```

**Error codes:**
| Code | Meaning |
|---|---|
| 403 | Caller is not Support or Super Admin |
| 404 | Notification not found |
| 409 | Notification is already DELIVERED or retry limit exceeded |

---

### POST /v1/notifications/retry-failed

| Field | Value |
|---|---|
| **Owner** | Notifications |
| **Related use case** | UC-NO-01 |
| **Description** | Retry all currently FAILED or PENDING Notifications. Intended for Support or scheduled operational recovery jobs. |

**Request body:** None

**Response (200 OK):**
```json
{
  "processed": "integer",
  "delivered": "integer",
  "failed": "integer"
}
```

**Error codes:**
| Code | Meaning |
|---|---|
| 403 | Caller is not Support, Platform Operator, or Super Admin |

---

## Reporting Service

### GET /v1/reports/sales

| Field | Value |
|---|---|
| **Owner** | Reporting |
| **Related use case** | UC-RE-01, UC-RE-02 |
| **Description** | Retrieve Sales Report. Scoped by organizerId for Organizer role. |

**Query parameters:** `organizerId` (uuid, optional — required for Organizer), `eventId` (uuid, optional), `dateId` (uuid, optional)

**Response (200 OK):**
```json
{
  "reports": [
    {
      "reportId": "uuid",
      "organizerId": "uuid",
      "eventId": "uuid",
      "dateId": "uuid",
      "totalTicketsSold": "integer",
      "totalRevenue": "decimal",
      "totalServiceFees": "decimal",
      "totalPayouts": "decimal",
      "generatedAt": "ISO-8601"
    }
  ]
}
```

**Error codes:**
| Code | Meaning |
|---|---|
| 403 | Organizer can only view own reports |

---

### GET /v1/reports/commissions

| Field | Value |
|---|---|
| **Owner** | Reporting |
| **Related use case** | UC-RE-01 |
| **Description** | Retrieve Commission Report. Finance and Super Admin only. |

**Query parameters:** `organizerId` (uuid, optional), `periodStart` (ISO-8601, optional), `periodEnd` (ISO-8601, optional)

**Response (200 OK):**
```json
{
  "reports": [
    {
      "reportId": "uuid",
      "organizerId": "uuid",
      "periodStart": "ISO-8601",
      "periodEnd": "ISO-8601",
      "totalServiceFees": "decimal",
      "generatedAt": "ISO-8601"
    }
  ]
}
```

**Error codes:**
| Code | Meaning |
|---|---|
| 403 | Only Finance, Platform Operator, or Super Admin |

---

### GET /v1/reports/access

| Field | Value |
|---|---|
| **Owner** | Reporting |
| **Related use case** | UC-RE-03 |
| **Description** | Retrieve Access/Validation Report for Venue Staff. |

**Query parameters:** `eventId` (uuid, required), `dateId` (uuid, optional)

**Response (200 OK):**
```json
{
  "eventId": "uuid",
  "dateId": "uuid | null",
  "totalValidations": "integer",
  "succeeded": "integer",
  "failed": "integer",
  "failureBreakdown": {
    "ALREADY_USED": "integer",
    "WRONG_EVENT": "integer",
    "EXPIRED": "integer",
    "INVALIDATED": "integer"
  },
  "offlineScans": "integer",
  "conflictsDetected": "integer"
}
```

**Error codes:**
| Code | Meaning |
|---|---|
| 404 | Event not found |
| 403 | Caller is not Venue Staff, Organizer, Platform Operator, or Super Admin for this Event |
