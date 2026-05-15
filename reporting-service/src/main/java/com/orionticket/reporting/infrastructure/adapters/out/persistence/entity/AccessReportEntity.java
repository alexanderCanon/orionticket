package com.orionticket.reporting.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "access_report")
public class AccessReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID reportId;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "date_id")
    private UUID dateId;

    @Column(name = "total_validations")
    private Integer totalValidations;

    @Column(name = "succeeded")
    private Integer succeeded;

    @Column(name = "failed")
    private Integer failed;

    @Column(name = "failure_breakdown", columnDefinition = "TEXT")
    private String failureBreakdown;

    @Column(name = "offline_scans")
    private Integer offlineScans;

    @Column(name = "conflicts_detected")
    private Integer conflictsDetected;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;

    public AccessReportEntity() {
    }

    public UUID getReportId() {
        return reportId;
    }

    public void setReportId(UUID reportId) {
        this.reportId = reportId;
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

    public Integer getTotalValidations() {
        return totalValidations;
    }

    public void setTotalValidations(Integer totalValidations) {
        this.totalValidations = totalValidations;
    }

    public Integer getSucceeded() {
        return succeeded;
    }

    public void setSucceeded(Integer succeeded) {
        this.succeeded = succeeded;
    }

    public Integer getFailed() {
        return failed;
    }

    public void setFailed(Integer failed) {
        this.failed = failed;
    }

    public String getFailureBreakdown() {
        return failureBreakdown;
    }

    public void setFailureBreakdown(String failureBreakdown) {
        this.failureBreakdown = failureBreakdown;
    }

    public Integer getOfflineScans() {
        return offlineScans;
    }

    public void setOfflineScans(Integer offlineScans) {
        this.offlineScans = offlineScans;
    }

    public Integer getConflictsDetected() {
        return conflictsDetected;
    }

    public void setConflictsDetected(Integer conflictsDetected) {
        this.conflictsDetected = conflictsDetected;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Instant generatedAt) {
        this.generatedAt = generatedAt;
    }
}