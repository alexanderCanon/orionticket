package com.orionticket.events.domain.port.out;

import com.orionticket.events.domain.model.Venue;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VenueRepositoryPort {
    Venue save(Venue venue);
    Optional<Venue> findById(UUID venueId);
    List<Venue> findAllByOrganizerId(UUID organizerId);
}
