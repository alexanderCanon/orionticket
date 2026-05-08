package com.orionticket.events.application.service;

import com.orionticket.events.application.port.in.VenueManagementUseCase;
import com.orionticket.events.domain.model.Venue;
import com.orionticket.events.domain.port.out.EventPublisherPort;
import com.orionticket.events.domain.port.out.VenueRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VenueManagementService implements VenueManagementUseCase {

    private final VenueRepositoryPort venueRepositoryPort;
    private final EventPublisherPort eventPublisherPort;

    @Override
    public Venue createVenue(UUID organizerId, String name, String address, Integer capacity) {
        Venue venue = Venue.create(organizerId, name, address, capacity);
        Venue savedVenue = venueRepositoryPort.save(venue);
        
        eventPublisherPort.publishVenueCreated(savedVenue);
        return savedVenue;
    }

    @Override
    public List<Venue> getVenuesByOrganizer(UUID organizerId) {
        return venueRepositoryPort.findAllByOrganizerId(organizerId);
    }
}
