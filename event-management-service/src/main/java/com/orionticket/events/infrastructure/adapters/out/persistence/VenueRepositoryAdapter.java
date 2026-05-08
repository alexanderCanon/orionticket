package com.orionticket.events.infrastructure.adapters.out.persistence;

import com.orionticket.events.domain.model.Venue;
import com.orionticket.events.domain.port.out.VenueRepositoryPort;
import com.orionticket.events.infrastructure.adapters.out.persistence.entity.VenueJpaEntity;
import com.orionticket.events.infrastructure.adapters.out.persistence.mapper.VenueMapper;
import com.orionticket.events.infrastructure.adapters.out.persistence.repository.SpringDataVenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class VenueRepositoryAdapter implements VenueRepositoryPort {

    private final SpringDataVenueRepository repository;

    @Override
    public Venue save(Venue venue) {
        VenueJpaEntity entity = VenueMapper.toEntity(venue);
        return VenueMapper.toDomain(repository.save(entity));
    }

    @Override
    public Optional<Venue> findById(UUID venueId) {
        return repository.findById(venueId).map(VenueMapper::toDomain);
    }

    @Override
    public List<Venue> findAllByOrganizerId(UUID organizerId) {
        return repository.findAllByOrganizerId(organizerId).stream()
                .map(VenueMapper::toDomain)
                .collect(Collectors.toList());
    }
}
