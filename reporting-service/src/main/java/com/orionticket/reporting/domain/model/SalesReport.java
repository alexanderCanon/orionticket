package com.orionticket.reporting.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class SalesReport {

    private final UUID reportId;
    private final UUID organizerId;
    private final UUID eventId;
    private final UUID dateId;
    private final Integer totalTicketsSold;
    private final BigDecimal totalRevenue;
    private final BigDecimal totalServiceFees;
    private final BigDecimal totalPayouts;
    private final Instant generatedAt;

    public SalesReport(UUID reportId, UUID organizerId, UUID eventId, UUID dateId,
                       Integer totalTicketsSold, BigDecimal totalRevenue,
                       BigDecimal totalServiceFees, BigDecimal totalPayouts, Instant generatedAt) {
        this.reportId = reportId;
        this.organizerId = organizerId;
        this.eventId = eventId;
        this.dateId = dateId;
        this.totalTicketsSold = totalTicketsSold;
        this.totalRevenue = totalRevenue;
        this.totalServiceFees = totalServiceFees;
        this.totalPayouts = totalPayouts;
        this.generatedAt = generatedAt;
    }

    public UUID getReportId() {
        return reportId;
    }

    public UUID getOrganizerId() {
        return organizerId;
    }

    public UUID getEventId() {
        return eventId;
    }

    public UUID getDateId() {
        return dateId;
    }

    public Integer getTotalTicketsSold() {
        return totalTicketsSold;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public BigDecimal getTotalServiceFees() {
        return totalServiceFees;
    }

    public BigDecimal getTotalPayouts() {
        return totalPayouts;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }
}