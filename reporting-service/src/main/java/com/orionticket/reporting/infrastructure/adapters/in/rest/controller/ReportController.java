package com.orionticket.reporting.infrastructure.adapters.in.rest.controller;

import com.orionticket.reporting.application.port.in.AccessReportQueryPort;
import com.orionticket.reporting.application.port.in.CommissionReportQueryPort;
import com.orionticket.reporting.application.port.in.SalesReportQueryPort;
import com.orionticket.reporting.infrastructure.adapters.in.rest.dto.AccessReportResponse;
import com.orionticket.reporting.infrastructure.adapters.in.rest.dto.CommissionReportListResponse;
import com.orionticket.reporting.infrastructure.adapters.in.rest.dto.CommissionReportResponse;
import com.orionticket.reporting.infrastructure.adapters.in.rest.dto.SalesReportListResponse;
import com.orionticket.reporting.infrastructure.adapters.in.rest.dto.SalesReportResponse;
import com.orionticket.reporting.infrastructure.adapters.in.rest.mapper.ReportMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/reports")
@Tag(name = "Reporting", description = "Report generation and retrieval endpoints")
public class ReportController {

    private final SalesReportQueryPort salesReportQueryPort;
    private final CommissionReportQueryPort commissionReportQueryPort;
    private final AccessReportQueryPort accessReportQueryPort;
    private final ReportMapper reportMapper;

    public ReportController(SalesReportQueryPort salesReportQueryPort,
            CommissionReportQueryPort commissionReportQueryPort,
            AccessReportQueryPort accessReportQueryPort,
            ReportMapper reportMapper) {
        this.salesReportQueryPort = salesReportQueryPort;
        this.commissionReportQueryPort = commissionReportQueryPort;
        this.accessReportQueryPort = accessReportQueryPort;
        this.reportMapper = reportMapper;
    }

    @GetMapping("/sales")
    @Operation(summary = "Get Sales Report", description = "Retrieve sales reports scoped by organizer")
    public ResponseEntity<SalesReportListResponse> getSalesReports(
            @RequestParam(required = false) UUID organizerId,
            @RequestParam(required = false) UUID eventId,
            @RequestParam(required = false) UUID dateId) {

        List<SalesReportResponse> reports = reportMapper.toSalesReportResponseList(
                salesReportQueryPort.getSalesReports(organizerId, eventId, dateId));
        return ResponseEntity.ok(new SalesReportListResponse(reports));
    }

    @GetMapping("/commissions")
    @Operation(summary = "Get Commission Report", description = "Retrieve commission reports for platform/finance")
    public ResponseEntity<CommissionReportListResponse> getCommissionReports(
            @RequestParam(required = false) UUID organizerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant periodStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant periodEnd) {

        List<CommissionReportResponse> reports = reportMapper.toCommissionReportResponseList(
                commissionReportQueryPort.getCommissionReports(organizerId, periodStart, periodEnd));
        return ResponseEntity.ok(new CommissionReportListResponse(reports));
    }

    @GetMapping("/access")
    @Operation(summary = "Get Access Report", description = "Retrieve access/validation reports for venue staff")
    public ResponseEntity<AccessReportResponse> getAccessReport(
            @RequestParam UUID eventId,
            @RequestParam(required = false) UUID dateId) {

        AccessReportResponse report = reportMapper.toAccessReportResponse(
                accessReportQueryPort.getAccessReport(eventId, dateId));
        return ResponseEntity.ok(report);
    }
}