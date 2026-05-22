SET search_path TO payments;

-- V1__init_payments_schema.sql
-- Payments Service Schema

-- Payments table
CREATE TABLE payments (
    payment_id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    buyer_id UUID NOT NULL,
    amount NUMERIC(19, 4) NOT NULL,
    service_fee NUMERIC(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    method VARCHAR(20) NOT NULL CHECK (method IN ('CARD', 'TRANSFER')),
    status VARCHAR(20) NOT NULL CHECK (status IN ('INITIATED', 'AUTHORIZED', 'FAILED')),
    gateway_reference VARCHAR(255),
    idempotency_key VARCHAR(255) UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_payments_buyer_id ON payments(buyer_id);
CREATE INDEX idx_payments_status ON payments(status);

-- Payouts table
CREATE TABLE payouts (
    payout_id UUID PRIMARY KEY,
    organizer_id UUID NOT NULL,
    event_id UUID NOT NULL,
    date_id UUID NOT NULL,
    gross_amount NUMERIC(19, 4) NOT NULL,
    service_fee_total NUMERIC(19, 4) NOT NULL,
    net_amount NUMERIC(19, 4) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'PROCESSED', 'FAILED')),
    retry_count INTEGER NOT NULL DEFAULT 0,
    triggered_at TIMESTAMP WITH TIME ZONE NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_payouts_organizer_id ON payouts(organizer_id);
CREATE INDEX idx_payouts_event_id ON payouts(event_id);
CREATE INDEX idx_payouts_status ON payouts(status);

-- Comments for documentation
COMMENT ON TABLE payments IS 'Payment transactions processed through gateway';
COMMENT ON TABLE payouts IS 'Payouts to organizers after events';