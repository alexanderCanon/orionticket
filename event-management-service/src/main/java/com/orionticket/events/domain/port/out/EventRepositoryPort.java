package com.orionticket.events.domain.port.out;

import com.orionticket.events.domain.model.Event;
import java.util.Optional;
import java.util.UUID;

public interface EventRepositoryPort {
    Event save(Event event);
    Optional<Event> findById(UUID eventId);
}
