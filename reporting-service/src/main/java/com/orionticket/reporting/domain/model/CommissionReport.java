package com.orionticket.reporting.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class CommissionReport {

    private final UUID reportId;
    private final UUID organizerId;
    private final Instant periodStart;
    private final Instant periodEnd;
    private final BigDecimal totalServiceFees;
    private final Instant generatedAt;

    public CommissionReport(UUID reportId, UUID organizerId, Instant periodStart,
                           Instant periodEnd, BigDecimal totalServiceFees, Instant generatedAt) {
        this.reportId = reportId;
        this.organizerId = organizerId;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.totalServiceFees = totalServiceFees;
        this.generatedAt = generatedAt;
    }

    public UUID getReportId() {
        return reportId;
    }

    public UUID getOrganizerId() {
        return organizerId;
    }

    public Instant getPeriodStart() {
        return periodStart;
    }

    public Instant getPeriodEnd() {
        return periodEnd;
    }

    public BigDecimal getTotalServiceFees() {
        return totalServiceFees;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }
}