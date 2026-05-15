package com.orionticket.reporting.application.service;

import com.orionticket.reporting.application.port.in.ReportQueryPort;
import com.orionticket.reporting.domain.model.SalesReport;
import com.orionticket.reporting.domain.port.out.ReportRepository;

import java.util.List;
import java.util.UUID;

public class SalesReportQueryService implements ReportQueryPort {

    private final ReportRepository reportRepository;

    public SalesReportQueryService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @Override
    public List<SalesReport> getSalesReports(UUID organizerId, UUID eventId, UUID dateId) {
        return reportRepository.findSalesReports(organizerId, eventId, dateId);
    }
}