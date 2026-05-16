# ADR-019: Ticket Email Delivery with Resend

| Field | Value |
|---|---|
| **Date** | 2026-05-15 |
| **Status** | PROPOSED |

## Context

Ticket delivery is part of the documented v1 scope. The platform supports ticket delivery through Email, PDF, QR, wallet, mobile download, and direct download. Notifications is the bounded context responsible for external communications, while Ticket Issuance owns ticket creation and ticket lifecycle.

The current documentation says Notifications sends email, SMS, and WhatsApp messages, but it does not select an email provider or define where ticket email templates are rendered.

## Decision

Use **Resend** as the MVP email provider and keep the delivery responsibility inside the **Notifications Service**.

Ticket Issuance must not call Resend directly. It emits the ticket-related domain event and remains responsible for issuing tickets. Notifications consumes the event, creates a notification record, renders the email with Thymeleaf, sends it through Resend, and records delivery status.

## Responsibility Split

| Capability | Owner |
|---|---|
| Create ticket and QR metadata | Ticket Issuance |
| Emit `TicketIssued` | Ticket Issuance |
| Create notification from ticket event | Notifications |
| Render ticket email template | Notifications |
| Call Resend API | Notifications |
| Store notification status and retry count | Notifications |
| Emit `NotificationDispatched`, `NotificationDelivered`, `NotificationFailed` | Notifications |
| Manual retry of failed notifications | Notifications |

## Minimum Ticket Email Flow

1. Ticket Issuance consumes `PaymentAuthorized`.
2. Ticket Issuance creates the Ticket and emits `TicketIssued`.
3. Notifications consumes `TicketIssued`.
4. Notifications creates a Notification with channel `EMAIL` and template `ticket-issued`.
5. Notifications renders the Thymeleaf template with the ticket payload.
6. Notifications sends the email through Resend.
7. Notifications stores the provider response and emits the corresponding notification event.

## Required Configuration

| Variable | Owner | Purpose |
|---|---|---|
| `RESEND_API_KEY` | Notifications | API key used to call Resend. |
| `RESEND_FROM_EMAIL` | Notifications | Verified sender address. |
| `RESEND_FROM_NAME` | Notifications | Sender display name. |
| `NOTIFICATIONS_EMAIL_ENABLED` | Notifications | Enables or disables email dispatch. |
| `ORIONTICKET_PUBLIC_BASE_URL` | Notifications | Public base URL used to build ticket links. |

## Event Contract Impact

The existing `TicketIssued` schema is enough to identify the ticket, buyer, event, date, seat, and delivery channels. It is not enough by itself to send a buyer-facing email without either:

- adding email-ready fields to the event payload, such as `recipientEmail`, `eventName`, and `ticketUrl`; or
- letting Notifications resolve the missing data through service APIs or read models.

For the MVP, the preferred option is to include email-ready delivery fields in the event payload to keep the notification flow asynchronous and avoid synchronous calls during dispatch.

## Non-Goals for MVP

- PDF attachments.
- Embedded QR images in the email body.
- Multi-provider failover.
- Marketing campaign automation.
- SMS and WhatsApp provider integration.
- Advanced template editor in the admin UI.

## Implementation Notes

1. Add a Resend HTTP adapter in Notifications.
2. Add a Thymeleaf template named `ticket-issued`.
3. Store provider message ID and failure reason in the notification persistence model if not already available.
4. Make email dispatch idempotent by notification ID and source event ID.
5. Define retry and DLQ behavior using the RabbitMQ standard from ADR-014.
6. Update the `TicketIssued` event schema before relying on the event for production ticket emails.

## Related Documents

- [ADR-014: Message Broker Selection](ADR-014-message-broker.md)
- [Ticket Issuance Service Manual](../../../project/services/06-ticket-issuance.md)
- [Notifications Service Manual](../../../project/services/08-notifications.md)
- [Event Schemas](../event-schemas.md)
- [Use Case Catalog](../../phase-2/use-case-catalog.md)
