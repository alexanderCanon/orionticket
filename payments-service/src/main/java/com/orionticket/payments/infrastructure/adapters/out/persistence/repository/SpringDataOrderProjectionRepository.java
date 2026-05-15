package com.orionticket.payments.infrastructure.adapters.out.persistence.repository;

import com.orionticket.payments.infrastructure.adapters.out.persistence.entity.OrderProjectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataOrderProjectionRepository extends JpaRepository<OrderProjectionEntity, UUID> {

    List<OrderProjectionEntity> findByDateId(UUID dateId);
}
