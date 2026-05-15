package com.orionticket.reporting.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sales_report")
public class SalesReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID reportId;

    @Column(name = "organizer_id", nullable = false)
    private UUID organizerId;

    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "date_id")
    private UUID dateId;

    @Column(name = "total_tickets_sold")
    private Integer totalTicketsSold;

    @Column(name = "total_revenue", precision = 12, scale = 2)
    private BigDecimal totalRevenue;

    @Column(name = "total_service_fees", precision = 12, scale = 2)
    private BigDecimal totalServiceFees;

    @Column(name = "total_payouts", precision = 12, scale = 2)
    private BigDecimal totalPayouts;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;

    public SalesReportEntity() {
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

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public UUID getDateId() {
        return dateId;
    }

    public void setDateId(UUID dateId) {
        this.dateId = dateId;
    }

    public Integer getTotalTicketsSold() {
        return totalTicketsSold;
    }

    public void setTotalTicketsSold(Integer totalTicketsSold) {
        this.totalTicketsSold = totalTicketsSold;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getTotalServiceFees() {
        return totalServiceFees;
    }

    public void setTotalServiceFees(BigDecimal totalServiceFees) {
        this.totalServiceFees = totalServiceFees;
    }

    public BigDecimal getTotalPayouts() {
        return totalPayouts;
    }

    public void setTotalPayouts(BigDecimal totalPayouts) {
        this.totalPayouts = totalPayouts;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Instant generatedAt) {
        this.generatedAt = generatedAt;
    }
}