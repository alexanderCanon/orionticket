-- V2: Tabla de Órdenes (Orders)
-- Una orden es el resultado del checkout. Vinculada a una reserva de seating-inventory.
-- UNIQUE en reservation_id: idempotencia garantizada a nivel de DB (ADR-008).

CREATE TABLE orders (
    order_id            UUID          PRIMARY KEY,
    buyer_id            UUID          NOT NULL,
    event_id            UUID          NOT NULL,
    date_id             UUID          NOT NULL,
    reservation_id      UUID          NOT NULL,         -- ref: SeatingInventory.reservationId (sin FK externa)
    status              VARCHAR(30)   NOT NULL DEFAULT 'CREATED',
        -- CREATED | PAYMENT_INITIATED | CONFIRMED | EXPIRED | FAILED
    subtotal            NUMERIC(10,2) NOT NULL,
    promotion_id        UUID          REFERENCES promotions(promotion_id),  -- null si sin promoción
    promotion_discount  NUMERIC(10,2) NOT NULL DEFAULT 0,
    service_fee         NUMERIC(10,2) NOT NULL,
    total               NUMERIC(10,2) NOT NULL,
    currency            VARCHAR(3)    NOT NULL DEFAULT 'GTQ',
    created_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

-- Garantiza que solo puede existir UNA orden por reserva — defensa final de idempotencia (ADR-008)
CREATE UNIQUE INDEX idx_orders_reservation_id ON orders(reservation_id);
CREATE INDEX idx_orders_buyer_id ON orders(buyer_id);
CREATE INDEX idx_orders_status   ON orders(status);
