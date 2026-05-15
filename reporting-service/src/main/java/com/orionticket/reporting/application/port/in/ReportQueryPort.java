package com.orionticket.reporting.application.port.in;

import com.orionticket.reporting.domain.model.AccessReport;
import com.orionticket.reporting.domain.model.CommissionReport;
import com.orionticket.reporting.domain.model.SalesReport;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ReportQueryPort {

    List<SalesReport> getSalesReports(UUID organizerId, UUID eventId, UUID dateId);

    List<CommissionReport> getCommissionReports(UUID organizerId, Instant periodStart, Instant periodEnd);

    AccessReport getAccessReport(UUID eventId, UUID dateId);
}