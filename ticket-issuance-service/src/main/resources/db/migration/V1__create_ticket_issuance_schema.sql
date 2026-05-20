CREATE TABLE tickets (
    ticket_id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    buyer_id UUID NOT NULL,
    event_id UUID NOT NULL,
    date_id UUID NOT NULL,
    seat_id UUID NULL,
    type VARCHAR(32) NOT NULL,
    holder_name VARCHAR(255) NOT NULL,
    qr_code TEXT NOT NULL,
    qr_expires_at TIMESTAMPTZ NOT NULL,
    access_policy VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL,
    delivered_at TIMESTAMPTZ NULL,
    issued_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT chk_tickets_type CHECK (type IN ('MAPPED', 'GENERAL_ADMISSION')),
    CONSTRAINT chk_tickets_status CHECK (status IN ('ISSUED', 'CANCELED', 'INVALIDATED', 'USED')),
    CONSTRAINT chk_tickets_seat_by_type CHECK (
        (type = 'MAPPED' AND seat_id IS NOT NULL)
        OR (type = 'GENERAL_ADMISSION' AND seat_id IS NULL)
    )
);

CREATE TABLE ticket_delivery_channels (
    ticket_id UUID NOT NULL REFERENCES tickets(ticket_id),
    channel VARCHAR(32) NOT NULL,
    CONSTRAINT chk_ticket_delivery_channels_channel CHECK (channel IN ('EMAIL', 'PDF', 'QR', 'WALLET', 'DOWNLOAD'))
);

CREATE INDEX idx_tickets_buyer_id ON tickets (buyer_id);
CREATE INDEX idx_tickets_order_id ON tickets (order_id);
CREATE INDEX idx_tickets_event_date ON tickets (event_id, date_id);
CREATE INDEX idx_ticket_delivery_channels_ticket_id ON ticket_delivery_channels (ticket_id);
