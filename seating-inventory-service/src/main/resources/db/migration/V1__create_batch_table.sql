-- V1: Tabla de Tandas (Batches)
-- Un Batch es un grupo de asientos a un precio específico con cupo limitado.
-- Ejemplo: "Preventa - Zona A" con capacidad 500 a Q250 desde el 1 de mayo.

CREATE TABLE batches (
    batch_id           UUID          PRIMARY KEY,
    event_id           UUID          NOT NULL,           -- referencia al evento (ID externo, sin FK cross-service)
    date_id            UUID          NOT NULL,           -- referencia a la fecha del evento
    name               VARCHAR(255)  NOT NULL,
    price              NUMERIC(10,2) NOT NULL,
    currency           VARCHAR(3)    NOT NULL DEFAULT 'GTQ',
    capacity           INTEGER       NOT NULL,
    sold               INTEGER       NOT NULL DEFAULT 0, -- se incrementa atómicamente con cada reserva
    status             VARCHAR(50)   NOT NULL DEFAULT 'SCHEDULED',  -- SCHEDULED | ACTIVE | EXHAUSTED | EXPIRED
    scheduled_start_at TIMESTAMPTZ   NOT NULL,           -- cuándo empieza a venderse esta tanda
    created_at         TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

-- Índice para consultas por evento+fecha (el caso más común)
CREATE INDEX idx_batches_event_date ON batches(event_id, date_id);
-- Índice para el scheduler que activa batches por status
CREATE INDEX idx_batches_status     ON batches(status);
