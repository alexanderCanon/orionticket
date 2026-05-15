-- V2__add_projection_tables.sql
-- Local read-model projections consumed from domain events.
-- These tables allow Payments to work without synchronous calls to other services.

-- Order projection: populated from OrderCreated events (from Orders service).
-- Stores financial data needed to initiate a Payment and calculate Payouts.
CREATE TABLE order_projections (
    order_id      UUID PRIMARY KEY,
    buyer_id      UUID NOT NULL,
    event_id      UUID NOT NULL,
    date_id       UUID NOT NULL,
    total         NUMERIC(19, 4) NOT NULL,
    service_fee   NUMERIC(19, 4) NOT NULL,
    currency      VARCHAR(3)  NOT NULL,
    status        VARCHAR(30) NOT NULL,
    received_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_order_proj_date_id ON order_projections(date_id);
CREATE INDEX idx_order_proj_event_id ON order_projections(event_id);

COMMENT ON TABLE order_projections IS 'Local projection of OrderCreated events from the Orders service';

-- Date projection: populated from DateAdded events (from Event Management service).
-- Used by the payout scheduler to detect dates that have passed (ADR-009).
CREATE TABLE date_projections (
    date_id           UUID PRIMARY KEY,
    event_id          UUID NOT NULL,
    scheduled_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    payout_generated  BOOLEAN NOT NULL DEFAULT FALSE,
    received_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_date_proj_event_id ON date_projections(event_id);
CREATE INDEX idx_date_proj_scheduled_at ON date_projections(scheduled_at);
CREATE INDEX idx_date_proj_payout_generated ON date_projections(payout_generated);

COMMENT ON TABLE date_projections IS 'Local projection of DateAdded events; payout_generated tracks ADR-009 payout trigger';
