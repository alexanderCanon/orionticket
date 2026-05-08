package com.orionticket.events.infrastructure.adapters.out.persistence.repository;

import com.orionticket.events.infrastructure.adapters.out.persistence.entity.EventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataEventRepository extends JpaRepository<EventJpaEntity, UUID> {
}
