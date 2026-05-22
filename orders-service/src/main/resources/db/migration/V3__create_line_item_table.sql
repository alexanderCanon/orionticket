SET search_path TO orders;

-- V3: Tabla de Ítems de Línea (Line Items)
-- Un line item representa un asiento dentro de la orden, con el precio de su tanda.
-- Por diseño: una reserva = un asiento = un line item con quantity=1.

CREATE TABLE line_items (
    line_item_id  UUID          PRIMARY KEY,
    order_id      UUID          NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
    seat_id       UUID          NOT NULL,            -- ref: SeatingInventory.seatId (sin FK externa)
    batch_price   NUMERIC(10,2) NOT NULL,            -- precio de la tanda al momento de la orden
    quantity      INTEGER       NOT NULL DEFAULT 1,
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_line_items_order_id ON line_items(order_id);
