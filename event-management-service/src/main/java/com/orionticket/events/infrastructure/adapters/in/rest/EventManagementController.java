package com.orionticket.events.infrastructure.adapters.in.rest;

import com.orionticket.events.application.port.in.EventManagementUseCase;
import com.orionticket.events.domain.model.Event;
import com.orionticket.events.domain.model.EventDate;
import com.orionticket.events.infrastructure.adapters.in.rest.dto.AddEventDateRequest;
import com.orionticket.events.infrastructure.adapters.in.rest.dto.CreateEventRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/events")
@RequiredArgsConstructor
@Tag(name = "Events", description = "Event Management Endpoints")
public class EventManagementController {

    private final EventManagementUseCase eventManagementUseCase;

    // Nota: Extraer desde el token JWT (Spring Security) en el futuro
    private final UUID TEMPORARY_ORGANIZER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @PostMapping
    @Operation(summary = "Create a new event", description = "Creates a new event in DRAFT status")
    public ResponseEntity<Event> createEvent(@Valid @RequestBody CreateEventRequest request) {
        Event event = eventManagementUseCase.createEvent(
                TEMPORARY_ORGANIZER_ID,
                request.getName(),
                request.getDescription(),
                request.getCategory()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(event);
    }

    @PostMapping("/{eventId}/dates")
    @Operation(summary = "Add a date to an event", description = "Adds a date with venue and capacity to a DRAFT event")
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

    @PostMapping("/{eventId}/submit")
    @Operation(summary = "Submit event for review", description = "Transitions an event from DRAFT to UNDER_REVIEW")
    public ResponseEntity<Event> submitEventForReview(@PathVariable UUID eventId) {
        Event event = eventManagementUseCase.submitEventForReview(eventId, TEMPORARY_ORGANIZER_ID);
        return ResponseEntity.ok(event);
    }

    @PostMapping("/{eventId}/approve")
    @Operation(summary = "Approve event", description = "Transitions an event to RELEASED status. Only for Platform Operators.")
    public ResponseEntity<Event> approveEvent(@PathVariable UUID eventId) {
        // Nota: Extraer operatorId desde el token
        UUID operatorId = UUID.fromString("00000000-0000-0000-0000-000000000009");
        Event event = eventManagementUseCase.approveEvent(eventId, operatorId);
        return ResponseEntity.ok(event);
    }

    @PostMapping("/{eventId}/reject")
    @Operation(summary = "Reject event", description = "Rejects an event and returns it to DRAFT status. Only for Platform Operators.")
    public ResponseEntity<Event> rejectEvent(
            @PathVariable UUID eventId,
            @Valid @RequestBody com.orionticket.events.infrastructure.adapters.in.rest.dto.RejectEventRequest request) {
        // Nota: Extraer operatorId desde el token
        UUID operatorId = UUID.fromString("00000000-0000-0000-0000-000000000009");
        Event event = eventManagementUseCase.rejectEvent(eventId, operatorId, request.getReason());
        return ResponseEntity.ok(event);
    }

    @PostMapping("/{eventId}/cancel")
    @Operation(summary = "Cancel event", description = "Transitions an event to CANCELED status. Requires a cancellation reason.")
    public ResponseEntity<Event> cancelEvent(
            @PathVariable UUID eventId,
            @Valid @RequestBody com.orionticket.events.infrastructure.adapters.in.rest.dto.CancelEventRequest request) {
        Event event = eventManagementUseCase.cancelEvent(eventId, TEMPORARY_ORGANIZER_ID, request.getReason());
        return ResponseEntity.ok(event);
    }
}
