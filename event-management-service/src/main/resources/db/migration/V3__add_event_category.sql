SET search_path TO event_management;

-- Migración V3: Añade columna category a la tabla events.
-- El campo category es obligatorio según el ER Diagram (docs/phases/phase-3/er-diagrams/event-management.md).
-- Se usa 'GENERAL' como valor por defecto para no romper registros existentes.
ALTER TABLE events ADD COLUMN category VARCHAR(100) NOT NULL DEFAULT 'GENERAL';
