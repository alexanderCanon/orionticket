package com.orionticket.events.infrastructure.adapters.out.persistence.repository;

import com.orionticket.events.infrastructure.adapters.out.persistence.entity.EventJpaEntity;
import com.orionticket.events.infrastructure.adapters.out.persistence.entity.EventDateJpaEntity;
import com.orionticket.events.infrastructure.adapters.out.persistence.entity.VenueJpaEntity;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public interface SpringDataEventRepository
        extends JpaRepository<EventJpaEntity, UUID>, JpaSpecificationExecutor<EventJpaEntity> {

    default Page<EventJpaEntity> findCatalog(
            String category,
            UUID organizerId,
            String city,
            LocalDate date,
            Pageable pageable) {
        return findAll((root, query, cb) -> {
            if (query != null) {
                query.distinct(true);
            }

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), "RELEASED"));

            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (organizerId != null) {
                predicates.add(cb.equal(root.get("organizerId"), organizerId));
            }

            Join<EventJpaEntity, EventDateJpaEntity> dateJoin = null;
            if (city != null && !city.isBlank()) {
                dateJoin = root.join("dates", JoinType.LEFT);
                var venueRoot = query.from(VenueJpaEntity.class);
                predicates.add(cb.equal(dateJoin.get("venueId"), venueRoot.get("venueId")));
                predicates.add(cb.equal(venueRoot.get("city"), city));
            }
            if (date != null) {
                if (dateJoin == null) {
                    dateJoin = root.join("dates", JoinType.LEFT);
                }
                var start = date.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
                var end = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
                predicates.add(cb.greaterThanOrEqualTo(dateJoin.get("scheduledAt"), start));
                predicates.add(cb.lessThan(dateJoin.get("scheduledAt"), end));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        }, pageable);
    }
}
