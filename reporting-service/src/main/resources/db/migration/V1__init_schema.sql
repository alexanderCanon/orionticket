-- V1__init_schema.sql
-- Reporting Service initial schema

CREATE TABLE sales_report (
    report_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organizer_id UUID NOT NULL,
    event_id UUID,
    date_id UUID,
    total_tickets_sold INTEGER,
    total_revenue DECIMAL(12, 2),
    total_service_fees DECIMAL(12, 2),
    total_payouts DECIMAL(12, 2),
    generated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE commission_report (
    report_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organizer_id UUID NOT NULL,
    period_start TIMESTAMP WITH TIME ZONE NOT NULL,
    period_end TIMESTAMP WITH TIME ZONE NOT NULL,
    total_service_fees DECIMAL(12, 2),
    generated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE access_report (
    report_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL,
    date_id UUID,
    total_validations INTEGER,
    succeeded INTEGER,
    failed INTEGER,
    failure_breakdown TEXT,
    offline_scans INTEGER,
    conflicts_detected INTEGER,
    generated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_sales_report_organizer ON sales_report(organizer_id);
CREATE INDEX idx_sales_report_event ON sales_report(event_id);
CREATE INDEX idx_sales_report_date ON sales_report(date_id);

CREATE INDEX idx_commission_report_organizer ON commission_report(organizer_id);
CREATE INDEX idx_commission_report_period ON commission_report(period_start, period_end);

CREATE INDEX idx_access_report_event ON access_report(event_id);
CREATE INDEX idx_access_report_date ON access_report(date_id);