-- V3: Tabla de Reservas (Reservations)
-- Una Reservation es el "hold" temporal de un asiento por 10 minutos.
-- Si el comprador paga → el asiento pasa a SOLD y la reserva queda vinculada.
-- Si no paga en 10 min → el job de expiración libera el asiento.

CREATE TABLE reservations (
    reservation_id UUID        PRIMARY KEY,
    seat_id        UUID        NOT NULL REFERENCES seats(seat_id),
    buyer_id       UUID        NOT NULL,   -- ID del comprador (referencia externa a Identity Service)
    event_id       UUID        NOT NULL,
    date_id        UUID        NOT NULL,
    batch_id       UUID        NOT NULL,   -- guardado para decrementar batch.sold al expirar
    expires_at     TIMESTAMPTZ NOT NULL,   -- ahora + 10 minutos al momento de crear
    status         VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE | EXPIRED | RELEASED
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Índice para encontrar la reserva activa de un asiento (usado en el job de expiración)
CREATE INDEX idx_reservations_seat    ON reservations(seat_id);
-- Índice para el job de expiración: busca reservas donde expires_at <= now()
CREATE INDEX idx_reservations_expires ON reservations(expires_at);
-- Índice para filtrar por status
CREATE INDEX idx_reservations_status  ON reservations(status);

-- ÍNDICE ÚNICO PARCIAL: la defensa definitiva contra overbooking a nivel de base de datos.
-- Garantiza que solo puede existir UNA reserva con status='ACTIVE' para el mismo seat_id.
-- Si el SELECT FOR UPDATE falla (bug de concurrencia), esta constraint rechaza el INSERT.
-- "Partial index" = solo aplica a filas donde status = 'ACTIVE'. Las EXPIRED/RELEASED no cuentan.
CREATE UNIQUE INDEX idx_reservations_one_active_per_seat
    ON reservations(seat_id)
    WHERE status = 'ACTIVE';
