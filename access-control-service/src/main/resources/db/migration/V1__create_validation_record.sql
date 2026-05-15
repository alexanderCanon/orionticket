-- Access Control Service - Initial Schema
-- Version: 1
-- Description: Create validation_record table for door validation records

CREATE TABLE validation_record (
    validation_id       UUID PRIMARY KEY,
    ticket_id           UUID NOT NULL,
    validator_device_id VARCHAR(100) NOT NULL,
    event_id            UUID NOT NULL,
    date_id             UUID NOT NULL,
    attempted_at        TIMESTAMP NOT NULL,
    result              VARCHAR(20) NOT NULL,
    failure_reason      VARCHAR(50),
    is_offline          BOOLEAN NOT NULL DEFAULT FALSE,
    synced_at           TIMESTAMP,
    conflict_detected   BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_validation_record_ticket_id
    ON validation_record(ticket_id);

CREATE INDEX idx_validation_record_event_date
    ON validation_record(event_id, date_id);

CREATE INDEX idx_validation_record_validator_device
    ON validation_record(validator_device_id);

CREATE INDEX idx_validation_record_result
    ON validation_record(result);