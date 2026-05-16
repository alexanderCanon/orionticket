package com.orionticket.events.infrastructure.adapters.in.rest;

import com.orionticket.events.application.port.in.EventManagementUseCase;
import com.orionticket.events.domain.model.Event;
import com.orionticket.events.domain.model.EventDate;
import com.orionticket.events.infrastructure.adapters.in.rest.dto.AddEventDateRequest;
import com.orionticket.events.infrastructure.adapters.in.rest.dto.CreateEventRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    // Extract from the JWT once the authentication context is wired.
    private final UUID TEMPORARY_ORGANIZER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @PostMapping
    @Operation(summary = "Create a new event", description = "Creates a new event in DRAFT status")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Event created"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
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
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Date added"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Event or venue not found"),
            @ApiResponse(responseCode = "409", description = "Event cannot be modified in its current state")
    })
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event submitted for review"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "409", description = "Event cannot be submitted from its current state")
    })
    public ResponseEntity<Event> submitEventForReview(@PathVariable UUID eventId) {
        Event event = eventManagementUseCase.submitEventForReview(eventId, TEMPORARY_ORGANIZER_ID);
        return ResponseEntity.ok(event);
    }

    @PostMapping("/{eventId}/approve")
    @Operation(summary = "Approve event", description = "Transitions an event to RELEASED status. Only for Platform Operators.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event approved"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "409", description = "Event cannot be approved from its current state")
    })
    public ResponseEntity<Event> approveEvent(@PathVariable UUID eventId) {
        // Extract the operator ID from the JWT once role-based access is wired.
        UUID operatorId = UUID.fromString("00000000-0000-0000-0000-000000000009");
        Event event = eventManagementUseCase.approveEvent(eventId, operatorId);
        return ResponseEntity.ok(event);
    }

    @PostMapping("/{eventId}/reject")
    @Operation(summary = "Reject event", description = "Rejects an event and returns it to DRAFT status. Only for Platform Operators.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event rejected"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "409", description = "Event cannot be rejected from its current state")
    })
    public ResponseEntity<Event> rejectEvent(
            @PathVariable UUID eventId,
            @Valid @RequestBody com.orionticket.events.infrastructure.adapters.in.rest.dto.RejectEventRequest request) {
        // Extract the operator ID from the JWT once role-based access is wired.
        UUID operatorId = UUID.fromString("00000000-0000-0000-0000-000000000009");
        Event event = eventManagementUseCase.rejectEvent(eventId, operatorId, request.getReason());
        return ResponseEntity.ok(event);
    }

    @PostMapping("/{eventId}/cancel")
    @Operation(summary = "Cancel event", description = "Transitions an event to CANCELED status. Requires a cancellation reason.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event canceled"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "409", description = "Event cannot be canceled from its current state")
    })
    public ResponseEntity<Event> cancelEvent(
            @PathVariable UUID eventId,
            @Valid @RequestBody com.orionticket.events.infrastructure.adapters.in.rest.dto.CancelEventRequest request) {
        Event event = eventManagementUseCase.cancelEvent(eventId, TEMPORARY_ORGANIZER_ID, request.getReason());
        return ResponseEntity.ok(event);
    }
}
