package com.orionticket.reporting.application.service;

import com.orionticket.reporting.application.port.in.CommissionReportQueryPort;
import com.orionticket.reporting.domain.model.CommissionReport;
import com.orionticket.reporting.domain.port.out.ReportRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class CommissionReportQueryService implements CommissionReportQueryPort {

    private final ReportRepository reportRepository;

    public CommissionReportQueryService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @Override
    public List<CommissionReport> getCommissionReports(UUID organizerId, Instant periodStart, Instant periodEnd) {
        return reportRepository.findCommissionReports(organizerId, periodStart, periodEnd);
    }
}