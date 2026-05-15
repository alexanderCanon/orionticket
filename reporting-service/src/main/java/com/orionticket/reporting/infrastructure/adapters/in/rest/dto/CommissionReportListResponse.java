package com.orionticket.reporting.infrastructure.adapters.in.rest.dto;

import java.util.List;

public class CommissionReportListResponse {

    private List<CommissionReportResponse> reports;

    public CommissionReportListResponse() {
    }

    public CommissionReportListResponse(List<CommissionReportResponse> reports) {
        this.reports = reports;
    }

    public List<CommissionReportResponse> getReports() {
        return reports;
    }

    public void setReports(List<CommissionReportResponse> reports) {
        this.reports = reports;
    }
}