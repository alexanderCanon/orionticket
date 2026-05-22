SET search_path TO notifications;

ALTER TABLE notifications ADD COLUMN provider_message_id VARCHAR(255);
ALTER TABLE notifications ADD COLUMN failure_reason TEXT;
