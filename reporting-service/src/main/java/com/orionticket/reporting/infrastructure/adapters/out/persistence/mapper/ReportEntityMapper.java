package com.orionticket.reporting.infrastructure.adapters.out.persistence.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orionticket.reporting.domain.model.AccessReport;
import com.orionticket.reporting.domain.model.CommissionReport;
import com.orionticket.reporting.domain.model.SalesReport;
import com.orionticket.reporting.infrastructure.adapters.out.persistence.entity.AccessReportEntity;
import com.orionticket.reporting.infrastructure.adapters.out.persistence.entity.CommissionReportEntity;
import com.orionticket.reporting.infrastructure.adapters.out.persistence.entity.SalesReportEntity;

import java.util.Map;

public class ReportEntityMapper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public SalesReport toDomain(SalesReportEntity entity) {
        if (entity == null) {
            return null;
        }
        return new SalesReport(
                entity.getReportId(),
                entity.getOrganizerId(),
                entity.getEventId(),
                entity.getDateId(),
                entity.getTotalTicketsSold(),
                entity.getTotalRevenue(),
                entity.getTotalServiceFees(),
                entity.getTotalPayouts(),
                entity.getGeneratedAt()
        );
    }

    public SalesReportEntity toEntity(SalesReport domain) {
        if (domain == null) {
            return null;
        }
        SalesReportEntity entity = new SalesReportEntity();
        entity.setReportId(domain.getReportId());
        entity.setOrganizerId(domain.getOrganizerId());
        entity.setEventId(domain.getEventId());
        entity.setDateId(domain.getDateId());
        entity.setTotalTicketsSold(domain.getTotalTicketsSold());
        entity.setTotalRevenue(domain.getTotalRevenue());
        entity.setTotalServiceFees(domain.getTotalServiceFees());
        entity.setTotalPayouts(domain.getTotalPayouts());
        entity.setGeneratedAt(domain.getGeneratedAt());
        return entity;
    }

    public CommissionReport toDomain(CommissionReportEntity entity) {
        if (entity == null) {
            return null;
        }
        return new CommissionReport(
                entity.getReportId(),
                entity.getOrganizerId(),
                entity.getPeriodStart(),
                entity.getPeriodEnd(),
                entity.getTotalServiceFees(),
                entity.getGeneratedAt()
        );
    }

    public CommissionReportEntity toEntity(CommissionReport domain) {
        if (domain == null) {
            return null;
        }
        CommissionReportEntity entity = new CommissionReportEntity();
        entity.setReportId(domain.getReportId());
        entity.setOrganizerId(domain.getOrganizerId());
        entity.setPeriodStart(domain.getPeriodStart());
        entity.setPeriodEnd(domain.getPeriodEnd());
        entity.setTotalServiceFees(domain.getTotalServiceFees());
        entity.setGeneratedAt(domain.getGeneratedAt());
        return entity;
    }

    public AccessReport toDomain(AccessReportEntity entity) {
        if (entity == null) {
            return null;
        }
        Map<String, Integer> failureBreakdown = null;
        if (entity.getFailureBreakdown() != null) {
            try {
                failureBreakdown = objectMapper.readValue(entity.getFailureBreakdown(),
                        new TypeReference<Map<String, Integer>>() {});
            } catch (JsonProcessingException e) {
                failureBreakdown = null;
            }
        }
        return new AccessReport(
                entity.getReportId(),
                entity.getEventId(),
                entity.getDateId(),
                entity.getTotalValidations(),
                entity.getSucceeded(),
                entity.getFailed(),
                failureBreakdown,
                entity.getOfflineScans(),
                entity.getConflictsDetected(),
                entity.getGeneratedAt()
        );
    }

    public AccessReportEntity toEntity(AccessReport domain) {
        if (domain == null) {
            return null;
        }
        AccessReportEntity entity = new AccessReportEntity();
        entity.setReportId(domain.getReportId());
        entity.setEventId(domain.getEventId());
        entity.setDateId(domain.getDateId());
        entity.setTotalValidations(domain.getTotalValidations());
        entity.setSucceeded(domain.getSucceeded());
        entity.setFailed(domain.getFailed());
        if (domain.getFailureBreakdown() != null) {
            try {
                entity.setFailureBreakdown(objectMapper.writeValueAsString(domain.getFailureBreakdown()));
            } catch (JsonProcessingException e) {
                entity.setFailureBreakdown(null);
            }
        }
        entity.setOfflineScans(domain.getOfflineScans());
        entity.setConflictsDetected(domain.getConflictsDetected());
        entity.setGeneratedAt(domain.getGeneratedAt());
        return entity;
    }
}