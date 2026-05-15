package com.orionticket.reporting.infrastructure.adapters.out.persistence.repository;

import com.orionticket.reporting.infrastructure.adapters.out.persistence.entity.CommissionReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface CommissionReportRepository extends JpaRepository<CommissionReportEntity, UUID> {

    @Query("SELECT c FROM CommissionReportEntity c WHERE " +
           "(:organizerId IS NULL OR c.organizerId = :organizerId) AND " +
           "(:periodStart IS NULL OR c.periodStart >= :periodStart) AND " +
           "(:periodEnd IS NULL OR c.periodEnd <= :periodEnd)")
    List<CommissionReportEntity> findByFilters(
            @Param("organizerId") UUID organizerId,
            @Param("periodStart") Instant periodStart,
            @Param("periodEnd") Instant periodEnd);
}