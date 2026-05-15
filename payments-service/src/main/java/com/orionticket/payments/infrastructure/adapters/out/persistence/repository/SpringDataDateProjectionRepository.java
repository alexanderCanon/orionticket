package com.orionticket.payments.infrastructure.adapters.out.persistence.repository;

import com.orionticket.payments.infrastructure.adapters.out.persistence.entity.DateProjectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataDateProjectionRepository extends JpaRepository<DateProjectionEntity, UUID> {

    /** Find all Date projections whose scheduled time has passed and payout has not yet been generated. */
    List<DateProjectionEntity> findByScheduledAtBeforeAndPayoutGeneratedFalse(Instant now);
}
