package com.orionticket.reporting.infrastructure.adapters.in.rest.dto;

import java.util.List;

public class SalesReportListResponse {

    private List<SalesReportResponse> reports;

    public SalesReportListResponse() {
    }

    public SalesReportListResponse(List<SalesReportResponse> reports) {
        this.reports = reports;
    }

    public List<SalesReportResponse> getReports() {
        return reports;
    }

    public void setReports(List<SalesReportResponse> reports) {
        this.reports = reports;
    }
}