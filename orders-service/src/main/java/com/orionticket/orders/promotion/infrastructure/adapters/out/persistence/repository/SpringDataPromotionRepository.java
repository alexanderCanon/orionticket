package com.orionticket.orders.promotion.infrastructure.adapters.out.persistence.repository;

import com.orionticket.orders.promotion.infrastructure.adapters.out.persistence.entity.PromotionJpaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataPromotionRepository extends JpaRepository<PromotionJpaEntity, UUID> {

    Optional<PromotionJpaEntity> findByCodeAndEventId(String code, UUID eventId);

    // SELECT FOR UPDATE — lock pesimista para evitar race condition cuando dos buyers
    // usan el mismo código de promoción simultáneamente y quedarían usos negativos
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PromotionJpaEntity p WHERE p.promotionId = :id")
    Optional<PromotionJpaEntity> findByIdWithLock(@Param("id") UUID id);
}
