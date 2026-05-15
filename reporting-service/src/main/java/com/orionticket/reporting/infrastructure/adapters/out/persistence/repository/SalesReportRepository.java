package com.orionticket.reporting.infrastructure.adapters.out.persistence.repository;

import com.orionticket.reporting.infrastructure.adapters.out.persistence.entity.SalesReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SalesReportRepository extends JpaRepository<SalesReportEntity, UUID> {

    @Query("SELECT s FROM SalesReportEntity s WHERE " +
           "(:organizerId IS NULL OR s.organizerId = :organizerId) AND " +
           "(:eventId IS NULL OR s.eventId = :eventId) AND " +
           "(:dateId IS NULL OR s.dateId = :dateId)")
    List<SalesReportEntity> findByFilters(
            @Param("organizerId") UUID organizerId,
            @Param("eventId") UUID eventId,
            @Param("dateId") UUID dateId);
}