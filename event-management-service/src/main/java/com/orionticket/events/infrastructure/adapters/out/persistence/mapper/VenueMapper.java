package com.orionticket.events.infrastructure.adapters.out.persistence.mapper;

import com.orionticket.events.domain.model.Venue;
import com.orionticket.events.infrastructure.adapters.out.persistence.entity.VenueJpaEntity;

public class VenueMapper {

    public static Venue toDomain(VenueJpaEntity entity) {
        if (entity == null) return null;
        return Venue.builder()
                .venueId(entity.getVenueId())
                .organizerId(entity.getOrganizerId())
                .name(entity.getName())
                .address(entity.getAddress())
                .city(entity.getCity())
                .capacity(entity.getCapacity())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static VenueJpaEntity toEntity(Venue venue) {
        if (venue == null) return null;
        VenueJpaEntity entity = new VenueJpaEntity();
        entity.setVenueId(venue.getVenueId());
        entity.setOrganizerId(venue.getOrganizerId());
        entity.setName(venue.getName());
        entity.setAddress(venue.getAddress());
        entity.setCity(venue.getCity());
        entity.setCapacity(venue.getCapacity());
        entity.setCreatedAt(venue.getCreatedAt());
        return entity;
    }
}
