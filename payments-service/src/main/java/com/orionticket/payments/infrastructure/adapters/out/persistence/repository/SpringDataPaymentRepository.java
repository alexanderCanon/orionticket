package com.orionticket.payments.infrastructure.adapters.out.persistence.repository;

import com.orionticket.payments.infrastructure.adapters.out.persistence.entity.PaymentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataPaymentRepository extends JpaRepository<PaymentJpaEntity, UUID> {

    Optional<PaymentJpaEntity> findByIdempotencyKey(String idempotencyKey);

    Optional<PaymentJpaEntity> findByOrderId(UUID orderId);

    Optional<PaymentJpaEntity> findByGatewayReference(String gatewayReference);
}