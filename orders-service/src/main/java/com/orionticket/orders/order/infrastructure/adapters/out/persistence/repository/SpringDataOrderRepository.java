package com.orionticket.orders.order.infrastructure.adapters.out.persistence.repository;

import com.orionticket.orders.order.infrastructure.adapters.out.persistence.entity.OrderJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataOrderRepository extends JpaRepository<OrderJpaEntity, UUID> {
    Optional<OrderJpaEntity> findByReservationId(UUID reservationId);
    Page<OrderJpaEntity> findByBuyerId(UUID buyerId, Pageable pageable);
}
