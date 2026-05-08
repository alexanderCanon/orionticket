package com.orionticket.events.application.port.in;

import com.orionticket.events.domain.model.Venue;

import java.util.List;
import java.util.UUID;

public interface VenueManagementUseCase {
    Venue createVenue(UUID organizerId, String name, String address, Integer capacity);
    List<Venue> getVenuesByOrganizer(UUID organizerId);
}
