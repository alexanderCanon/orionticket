package com.orionticket.reporting.infrastructure.adapters.out.persistence.repository;

import com.orionticket.reporting.infrastructure.adapters.out.persistence.entity.AccessReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccessReportRepository extends JpaRepository<AccessReportEntity, UUID> {

    @Query("SELECT a FROM AccessReportEntity a WHERE a.eventId = :eventId AND " +
           "(:dateId IS NULL OR a.dateId = :dateId)")
    Optional<AccessReportEntity> findByEventIdAndDateId(
            @Param("eventId") UUID eventId,
            @Param("dateId") UUID dateId);
}