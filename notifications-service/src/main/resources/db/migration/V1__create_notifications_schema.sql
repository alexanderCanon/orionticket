SET search_path TO notifications;

CREATE TABLE notifications (
    notification_id UUID PRIMARY KEY,
    recipient_id UUID NOT NULL,
    channel VARCHAR(32) NOT NULL,
    template_id VARCHAR(120) NOT NULL,
    payload JSONB NOT NULL,
    status VARCHAR(32) NOT NULL,
    retry_count INTEGER NOT NULL DEFAULT 0,
    triggered_by VARCHAR(120) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT chk_notifications_channel CHECK (channel IN ('EMAIL', 'SMS', 'WHATSAPP')),
    CONSTRAINT chk_notifications_status CHECK (status IN ('PENDING', 'DISPATCHED', 'DELIVERED', 'FAILED')),
    CONSTRAINT chk_notifications_retry_count CHECK (retry_count >= 0)
);

CREATE INDEX idx_notifications_recipient_id ON notifications (recipient_id);
CREATE INDEX idx_notifications_status ON notifications (status);
CREATE INDEX idx_notifications_triggered_by ON notifications (triggered_by);
