package com.orionticket.reporting.application.port.in;

import com.orionticket.reporting.domain.model.SalesReport;
import java.util.List;
import java.util.UUID;

public interface SalesReportQueryPort {
    List<SalesReport> getSalesReports(UUID organizerId, UUID eventId, UUID dateId);
}
