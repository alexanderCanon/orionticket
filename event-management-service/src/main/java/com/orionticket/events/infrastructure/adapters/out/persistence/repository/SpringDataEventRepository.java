package com.orionticket.events.infrastructure.adapters.out.persistence.repository;

import com.orionticket.events.infrastructure.adapters.out.persistence.entity.EventJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.UUID;

public interface SpringDataEventRepository extends JpaRepository<EventJpaEntity, UUID> {

    @Query("SELECT DISTINCT e FROM EventJpaEntity e " +
           "LEFT JOIN e.dates d " +
           "LEFT JOIN VenueJpaEntity v ON d.venueId = v.venueId " +
           "WHERE e.status = 'RELEASED' " +
           "AND (:category IS NULL OR e.category = :category) " +
           "AND (:organizerId IS NULL OR e.organizerId = :organizerId) " +
           "AND (:city IS NULL OR v.city = :city) " +
           "AND (:date IS NULL OR CAST(d.scheduledAt AS date) = :date)")
    Page<EventJpaEntity> findCatalog(
            @Param("category") String category, 
            @Param("organizerId") UUID organizerId, 
            @Param("city") String city,
            @Param("date") LocalDate date, 
            Pageable pageable);
}
