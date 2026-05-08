# Microservicio: Orders (Pedidos)

Responsable del proceso de checkout, la lógica de precios y la gestión de promociones.

---

## 1. Responsabilidades
- Creación de Pedidos (Orders) vinculados a reservas.
- Resolución de precios (Batch + Promociones + Service Fee).
- Aplicación de códigos promocionales.
- Ciclo de vida del pedido (Created -> Paid -> Expired).

## 2. Documentación Técnica Específica
- **Casos de Uso:** [UC-OR-01, UC-OR-02](../../phases/phase-2/use-case-catalog.md).
- **Agregados:** [Order (con LineItems), Promotion](../../phases/phase-1/aggregate-definitions.md).
- **Contrato API:** [Orders Endpoints](../../phases/phase-3/service-contracts.md).
- **Diagrama ER:** [Modelo de Datos de Orders](../../phases/phase-3/er-diagrams/orders.md).
- **Decisión de Arquitectura:** [Pricing colapsado en Orders (ADR-011)](../../phases/phase-3/adrs/ADR-011-pricing-collapsed.md).

## 3. Eventos de Dominio
### Genera (Produce):
- `OrderCreated`, `OrderExpired`, `OrderConfirmed`.
- `PromotionCreated`, `PromotionExhausted`.
### Consume:
- `ReservationCreated` (para iniciar checkout).
- `PaymentAuthorized` (para confirmar pedido).
- `ReservationExpired` (para expirar pedido).

---

## 4. Estándares Aplicables
- [Estrategia de Idempotencia (ADR-008)](../../phases/phase-3/adrs/ADR-008-idempotency-key.md).
