package com.orionticket.orders.order.infrastructure.adapters.out.persistence.repository;

import com.orionticket.orders.order.infrastructure.adapters.out.persistence.entity.ReservationSnapshotJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataReservationSnapshotRepository
        extends JpaRepository<ReservationSnapshotJpaEntity, UUID> {
}
