package com.orionticket.reporting.infrastructure.adapters.in.rest.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class CommissionReportResponse {

    private UUID reportId;
    private UUID organizerId;
    private Instant periodStart;
    private Instant periodEnd;
    private BigDecimal totalServiceFees;
    private Instant generatedAt;

    public CommissionReportResponse() {
    }

    public UUID getReportId() {
        return reportId;
    }

    public void setReportId(UUID reportId) {
        this.reportId = reportId;
    }

    public UUID getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(UUID organizerId) {
        this.organizerId = organizerId;
    }

    public Instant getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(Instant periodStart) {
        this.periodStart = periodStart;
    }

    public Instant getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(Instant periodEnd) {
        this.periodEnd = periodEnd;
    }

    public BigDecimal getTotalServiceFees() {
        return totalServiceFees;
    }

    public void setTotalServiceFees(BigDecimal totalServiceFees) {
        this.totalServiceFees = totalServiceFees;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Instant generatedAt) {
        this.generatedAt = generatedAt;
    }
}