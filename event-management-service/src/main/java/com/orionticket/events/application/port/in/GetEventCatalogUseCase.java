package com.orionticket.events.application.port.in;

import com.orionticket.events.domain.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

public interface GetEventCatalogUseCase {
    Page<Event> getCatalog(String category, String city, LocalDate date, UUID organizerId, Pageable pageable);
}
