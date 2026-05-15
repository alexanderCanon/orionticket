package com.orionticket.reporting.application.service;

import com.orionticket.reporting.application.port.in.ReportQueryPort;
import com.orionticket.reporting.domain.model.AccessReport;
import com.orionticket.reporting.domain.port.out.ReportRepository;

import java.util.UUID;

public class AccessReportQueryService implements ReportQueryPort {

    private final ReportRepository reportRepository;

    public AccessReportQueryService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    @Override
    public AccessReport getAccessReport(UUID eventId, UUID dateId) {
        return reportRepository.findAccessReport(eventId, dateId).orElse(null);
    }
}