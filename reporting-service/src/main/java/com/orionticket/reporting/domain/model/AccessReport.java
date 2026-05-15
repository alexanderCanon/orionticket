package com.orionticket.reporting.domain.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class AccessReport {

    private final UUID reportId;
    private final UUID eventId;
    private final UUID dateId;
    private final Integer totalValidations;
    private final Integer succeeded;
    private final Integer failed;
    private final Map<String, Integer> failureBreakdown;
    private final Integer offlineScans;
    private final Integer conflictsDetected;
    private final Instant generatedAt;

    public AccessReport(UUID reportId, UUID eventId, UUID dateId, Integer totalValidations,
                       Integer succeeded, Integer failed, Map<String, Integer> failureBreakdown,
                       Integer offlineScans, Integer conflictsDetected, Instant generatedAt) {
        this.reportId = reportId;
        this.eventId = eventId;
        this.dateId = dateId;
        this.totalValidations = totalValidations;
        this.succeeded = succeeded;
        this.failed = failed;
        this.failureBreakdown = failureBreakdown;
        this.offlineScans = offlineScans;
        this.conflictsDetected = conflictsDetected;
        this.generatedAt = generatedAt;
    }

    public UUID getReportId() {
        return reportId;
    }

    public UUID getEventId() {
        return eventId;
    }

    public UUID getDateId() {
        return dateId;
    }

    public Integer getTotalValidations() {
        return totalValidations;
    }

    public Integer getSucceeded() {
        return succeeded;
    }

    public Integer getFailed() {
        return failed;
    }

    public Map<String, Integer> getFailureBreakdown() {
        return failureBreakdown;
    }

    public Integer getOfflineScans() {
        return offlineScans;
    }

    public Integer getConflictsDetected() {
        return conflictsDetected;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }
}