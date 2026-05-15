package com.orionticket.reporting.infrastructure.adapters.in.rest.mapper;

import com.orionticket.reporting.domain.model.AccessReport;
import com.orionticket.reporting.domain.model.CommissionReport;
import com.orionticket.reporting.domain.model.SalesReport;
import com.orionticket.reporting.infrastructure.adapters.in.rest.dto.AccessReportResponse;
import com.orionticket.reporting.infrastructure.adapters.in.rest.dto.CommissionReportResponse;
import com.orionticket.reporting.infrastructure.adapters.in.rest.dto.SalesReportResponse;

import java.util.List;

public class ReportMapper {

    public SalesReportResponse toSalesReportResponse(SalesReport salesReport) {
        if (salesReport == null) {
            return null;
        }
        SalesReportResponse response = new SalesReportResponse();
        response.setReportId(salesReport.getReportId());
        response.setOrganizerId(salesReport.getOrganizerId());
        response.setEventId(salesReport.getEventId());
        response.setDateId(salesReport.getDateId());
        response.setTotalTicketsSold(salesReport.getTotalTicketsSold());
        response.setTotalRevenue(salesReport.getTotalRevenue());
        response.setTotalServiceFees(salesReport.getTotalServiceFees());
        response.setTotalPayouts(salesReport.getTotalPayouts());
        response.setGeneratedAt(salesReport.getGeneratedAt());
        return response;
    }

    public List<SalesReportResponse> toSalesReportResponseList(List<SalesReport> reports) {
        return reports.stream()
                .map(this::toSalesReportResponse)
                .toList();
    }

    public CommissionReportResponse toCommissionReportResponse(CommissionReport commissionReport) {
        if (commissionReport == null) {
            return null;
        }
        CommissionReportResponse response = new CommissionReportResponse();
        response.setReportId(commissionReport.getReportId());
        response.setOrganizerId(commissionReport.getOrganizerId());
        response.setPeriodStart(commissionReport.getPeriodStart());
        response.setPeriodEnd(commissionReport.getPeriodEnd());
        response.setTotalServiceFees(commissionReport.getTotalServiceFees());
        response.setGeneratedAt(commissionReport.getGeneratedAt());
        return response;
    }

    public List<CommissionReportResponse> toCommissionReportResponseList(List<CommissionReport> reports) {
        return reports.stream()
                .map(this::toCommissionReportResponse)
                .toList();
    }

    public AccessReportResponse toAccessReportResponse(AccessReport accessReport) {
        if (accessReport == null) {
            return null;
        }
        AccessReportResponse response = new AccessReportResponse();
        response.setReportId(accessReport.getReportId());
        response.setEventId(accessReport.getEventId());
        response.setDateId(accessReport.getDateId());
        response.setTotalValidations(accessReport.getTotalValidations());
        response.setSucceeded(accessReport.getSucceeded());
        response.setFailed(accessReport.getFailed());
        response.setFailureBreakdown(accessReport.getFailureBreakdown());
        response.setOfflineScans(accessReport.getOfflineScans());
        response.setConflictsDetected(accessReport.getConflictsDetected());
        response.setGeneratedAt(accessReport.getGeneratedAt());
        return response;
    }
}