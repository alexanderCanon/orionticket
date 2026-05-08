package com.orionticket.events.domain.port.out;

import com.orionticket.events.domain.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface EventRepositoryPort {
    Event save(Event event);
    Optional<Event> findById(UUID eventId);
    Page<Event> findCatalog(String category, String city, LocalDate date, UUID organizerId, Pageable pageable);
}
