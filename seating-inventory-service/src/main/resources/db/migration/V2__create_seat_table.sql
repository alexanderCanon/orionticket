SET search_path TO seating_inventory;

-- V2: Tabla de Asientos (Seats)
-- Un Seat representa un lugar físico (o virtual para GA) en el evento.
-- Puede ser MAPPED (asiento numerado con zona/fila) o GENERAL_ADMISSION (sin asignación).
-- Siempre pertenece a un Batch (que define el precio y cupo).

CREATE TABLE seats (
    seat_id       UUID         PRIMARY KEY,
    event_id      UUID         NOT NULL,
    date_id       UUID         NOT NULL,
    batch_id      UUID         NOT NULL REFERENCES batches(batch_id),  -- precio y cupo vienen del batch
    zone          VARCHAR(100),           -- null para GENERAL_ADMISSION
    section       VARCHAR(100),           -- null para GENERAL_ADMISSION
    row_label     VARCHAR(50),            -- null para GENERAL_ADMISSION (se llama row_label para no colisionar con ROW keyword de SQL)
    seat_number   VARCHAR(50),            -- null para GENERAL_ADMISSION
    type          VARCHAR(30)  NOT NULL,  -- MAPPED | GENERAL_ADMISSION
    status        VARCHAR(30)  NOT NULL DEFAULT 'AVAILABLE',  -- AVAILABLE | RESERVED | SOLD | BLOCKED
    access_policy VARCHAR(255),           -- política de acceso al recinto (ej. "ONE_ENTRY_ONLY")
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Índice principal: buscar asientos por evento y fecha
CREATE INDEX idx_seats_event_date ON seats(event_id, date_id);
-- Índice para filtrar por status (consultas de disponibilidad)
CREATE INDEX idx_seats_status     ON seats(status);
-- Índice para encontrar todos los asientos de un batch
CREATE INDEX idx_seats_batch      ON seats(batch_id);
