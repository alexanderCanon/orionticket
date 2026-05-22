SET search_path TO orders;

-- V1: Tabla de Promociones (Promotions)
-- Collapse de Pricing en Orders según ADR-011. Gestionado completamente por este servicio.

CREATE TABLE promotions (
    promotion_id    UUID          PRIMARY KEY,
    event_id        UUID          NOT NULL,         -- referencia al evento (ID externo, sin FK cross-service)
    code            VARCHAR(50)   NOT NULL,
    discount_type   VARCHAR(20)   NOT NULL,         -- PERCENTAGE | FIXED
    discount_value  NUMERIC(10,2) NOT NULL,
    max_uses        INTEGER       NOT NULL,
    used_count      INTEGER       NOT NULL DEFAULT 0,
    status          VARCHAR(30)   NOT NULL DEFAULT 'ACTIVE', -- CREATED | ACTIVE | DEACTIVATED | EXHAUSTED
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

-- Índice único: un código solo puede existir una vez por evento
CREATE UNIQUE INDEX idx_promotions_code_event ON promotions(code, event_id);
-- Índice de status para consultas de validación rápida
CREATE INDEX idx_promotions_status ON promotions(status);
