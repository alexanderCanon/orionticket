package com.orionticket.events.infrastructure.adapters.in.rest;

import com.orionticket.events.application.port.in.EventManagementUseCase;
import com.orionticket.events.domain.model.Event;
import com.orionticket.events.domain.model.EventDate;
import com.orionticket.events.infrastructure.adapters.in.rest.dto.AddEventDateRequest;
import com.orionticket.events.infrastructure.adapters.in.rest.dto.CreateEventRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/events")
@RequiredArgsConstructor
public class EventManagementController {

    private final EventManagementUseCase eventManagementUseCase;

    // TODO: Extraer desde el token JWT (Spring Security) en el futuro
    private final UUID TEMPORARY_ORGANIZER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @PostMapping
    public ResponseEntity<Event> createEvent(@Valid @RequestBody CreateEventRequest request) {
        Event event = eventManagementUseCase.createEvent(
                TEMPORARY_ORGANIZER_ID, 
                request.getName(), 
                request.getDescription()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(event);
    }

    @PostMapping("/{eventId}/dates")
    public ResponseEntity<EventDate> addDateToEvent(
            @PathVariable UUID eventId,
            @Valid @RequestBody AddEventDateRequest request) {
            
        EventDate date = eventManagementUseCase.addDateToEvent(
                eventId,
                TEMPORARY_ORGANIZER_ID,
                request.getScheduledAt(),
                request.getVenueId(),
                request.getCapacity()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(date);
    }
}
