package com.orionticket.reporting.infrastructure.adapters.in.rest.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class AccessReportResponse {

    private UUID reportId;
    private UUID eventId;
    private UUID dateId;
    private Integer totalValidations;
    private Integer succeeded;
    private Integer failed;
    private Map<String, Integer> failureBreakdown;
    private Integer offlineScans;
    private Integer conflictsDetected;
    private Instant generatedAt;

    public AccessReportResponse() {
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

    public Map<String, Integer> getFailureBreakdown() {
        return failureBreakdown;
    }

    public void setFailureBreakdown(Map<String, Integer> failureBreakdown) {
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