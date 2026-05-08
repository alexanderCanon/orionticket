package com.orionticket.events.infrastructure.adapters.out.persistence.repository;

import com.orionticket.events.infrastructure.adapters.out.persistence.entity.VenueJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataVenueRepository extends JpaRepository<VenueJpaEntity, UUID> {
    List<VenueJpaEntity> findAllByOrganizerId(UUID organizerId);
}
