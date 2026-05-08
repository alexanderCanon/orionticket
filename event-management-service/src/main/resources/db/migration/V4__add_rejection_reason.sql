-- Migración V4: Añade columna rejection_reason a la tabla events.
-- El campo rejection_reason es requerido por el ER Diagram (docs/phases/phase-3/er-diagrams/event-management.md)
-- como "null unless rejected". Es nullable por diseño.
ALTER TABLE events ADD COLUMN rejection_reason TEXT;
