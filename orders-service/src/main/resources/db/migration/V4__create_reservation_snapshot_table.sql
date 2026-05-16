-- V4: Tabla de Snapshots de Reservas (Reservation Snapshots)
-- Copia local de reservas recibidas desde seating-inventory via evento ReservationCreated.
-- Patrón de snapshot: permite crear órdenes sin llamadas síncronas a otro servicio.
-- Incluye expires_at para detectar si la reserva ya venció al momento del checkout.

CREATE TABLE reservation_snapshots (
    reservation_id  UUID          PRIMARY KEY,
    seat_id         UUID          NOT NULL,
    batch_id        UUID          NOT NULL,
    batch_price     NUMERIC(10,2) NOT NULL,    -- precio vigente al momento de la reserva
    buyer_id        UUID          NOT NULL,
    event_id        UUID          NOT NULL,
    date_id         UUID          NOT NULL,
    expires_at      TIMESTAMPTZ   NOT NULL,    -- cuándo vence la reserva en seating-inventory
    received_at     TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);
