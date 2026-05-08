# Microservicio: Ticket Issuance (Emisión de Boletos)

Responsable de generar los boletos digitales y gestionar el ciclo de vida del QR dinámico.

---

## 1. Responsabilidades
- Generación de Boletos (Tickets) tras pago confirmado.
- Generación y rotación del QR dinámico (2 min TTL).
- Gestión de canales de entrega (Email, PDF, etc.).
- Cancelación e Invalidación de boletos.

## 2. Documentación Técnica Específica
- **Casos de Uso:** [UC-TI-01 al UC-TI-04](../../phases/phase-2/use-case-catalog.md).
- **Agregado:** [Ticket](../../phases/phase-1/aggregate-definitions.md).
- **Contrato API:** [Ticket Issuance Endpoints](../../phases/phase-3/service-contracts.md).
- **Diagrama ER:** [Modelo de Datos de Ticket Issuance](../../phases/phase-3/er-diagrams/ticket-issuance.md).
- **Decisión de Arquitectura:** [QR Dinámico con TTL (ADR-006)](../../phases/phase-3/adrs/ADR-006-dynamic-qr-ttl.md).

## 3. Eventos de Dominio
### Genera (Produce):
- `TicketIssued`, `TicketDelivered`.
- `TicketCanceled`, `TicketInvalidated`.
### Consume:
- `PaymentAuthorized` (disparador para emitir boleto).
- `ConflictDetected` (disparador para invalidar boleto por fraude).

---

## 4. Estándares Aplicables
- [BR-TI-01 al BR-TI-08: Reglas de Emisión](../../phases/phase-2/business-rules.md).
