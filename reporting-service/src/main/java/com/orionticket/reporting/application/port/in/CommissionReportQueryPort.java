package com.orionticket.reporting.application.port.in;

import com.orionticket.reporting.domain.model.CommissionReport;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface CommissionReportQueryPort {
    List<CommissionReport> getCommissionReports(UUID organizerId, Instant periodStart, Instant periodEnd);
}
