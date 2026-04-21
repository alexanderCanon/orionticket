# Microservicio: Payments (Pagos)

Responsable de la integración con pasarelas de pago y la liquidación de fondos a organizadores.

---

## 1. Responsabilidades
- Procesamiento de pagos (Tarjeta / Transferencia).
- Manejo de Webhooks de la pasarela de pago.
- Generación de Liquidaciones (Payouts) automáticas tras el evento.
- Conciliación financiera básica.

## 2. Documentación Técnica Específica
- **Casos de Uso:** [UC-PA-01, UC-PA-02, UC-PA-03](../docs/phases/phase-2/use-case-catalog.md).
- **Agregados:** [Payment, Payout](../docs/phases/phase-1/aggregate-definitions.md).
- **Contrato API:** [Payments Endpoints](../docs/phases/phase-3/service-contracts.md).
- **Diagrama ER:** [Modelo de Datos de Payments](../docs/phases/phase-3/er-diagrams/payments.md).
- **Decisión de Arquitectura:** [Liquidación post-fecha (ADR-009)](../docs/phases/phase-3/adrs/ADR-009-payout-after-date.md).

## 3. Eventos de Dominio
### Genera (Produce):
- `PaymentInitiated`, `PaymentAuthorized`, `PaymentFailed`.
- `PayoutGenerated`, `PayoutProcessed`.
### Consume:
- `OrderCreated` (para habilitar el pago).
- `DateAdded` (para programar liquidación futura).

---

## 4. Estándares Aplicables
- [BR-PA-01 al BR-PA-08: Reglas de Pago](../docs/phases/phase-2/business-rules.md).
