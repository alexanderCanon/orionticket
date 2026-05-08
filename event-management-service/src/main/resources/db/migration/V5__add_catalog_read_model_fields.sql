-- Migración V5: Añade columnas para el Catálogo (Read Model)
ALTER TABLE venues ADD COLUMN city VARCHAR(100) DEFAULT 'Desconocida';
ALTER TABLE events ADD COLUMN organizer_name VARCHAR(255) DEFAULT 'Organizador Desconocido';
