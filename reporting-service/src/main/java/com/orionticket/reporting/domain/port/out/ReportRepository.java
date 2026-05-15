package com.orionticket.reporting.domain.port.out;

import com.orionticket.reporting.domain.model.AccessReport;
import com.orionticket.reporting.domain.model.CommissionReport;
import com.orionticket.reporting.domain.model.SalesReport;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReportRepository {

    List<SalesReport> findSalesReports(UUID organizerId, UUID eventId, UUID dateId);

    List<CommissionReport> findCommissionReports(UUID organizerId, Instant periodStart, Instant periodEnd);

    Optional<AccessReport> findAccessReport(UUID eventId, UUID dateId);

    void save(SalesReport salesReport);

    void save(CommissionReport commissionReport);

    void save(AccessReport accessReport);
}