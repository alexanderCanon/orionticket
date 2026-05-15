package com.orionticket.payments.infrastructure.adapters.out.persistence.repository;

import com.orionticket.payments.infrastructure.adapters.out.persistence.entity.PayoutJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataPayoutRepository extends JpaRepository<PayoutJpaEntity, UUID> {

    List<PayoutJpaEntity> findByOrganizerId(UUID organizerId);

    List<PayoutJpaEntity> findByOrganizerIdAndStatus(UUID organizerId, PayoutJpaEntity.PayoutStatus status);
}