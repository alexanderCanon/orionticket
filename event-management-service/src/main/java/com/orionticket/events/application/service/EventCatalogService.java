package com.orionticket.events.application.service;

import com.orionticket.events.application.port.in.GetEventCatalogUseCase;
import com.orionticket.events.domain.model.Event;
import com.orionticket.events.domain.port.out.EventRepositoryPort;
import com.orionticket.events.domain.port.out.VenueRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventCatalogService implements GetEventCatalogUseCase {

    private final EventRepositoryPort eventRepositoryPort;
    private final VenueRepositoryPort venueRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public Page<Event> getCatalog(String category, String city, LocalDate date, UUID organizerId, Pageable pageable) {
        Page<Event> events = eventRepositoryPort.findCatalog(category, city, date, organizerId, pageable);
        
        // Populate additional info for catalog
        events.forEach(event -> {
            event.getDates().forEach(eventDate -> {
                venueRepositoryPort.findById(eventDate.getVenueId())
                        .ifPresent(v -> eventDate.setVenueName(v.getName()));
                
                // Placeholder: availableSeats = capacity until Seating/Inventory service is integrated
                if (eventDate.getAvailableSeats() == null) {
                    eventDate.setAvailableSeats(eventDate.getCapacity());
                }
            });
        });
        
        return events;
    }
}
