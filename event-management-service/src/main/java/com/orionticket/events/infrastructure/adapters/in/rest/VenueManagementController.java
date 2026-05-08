package com.orionticket.events.infrastructure.adapters.in.rest;

import com.orionticket.events.application.port.in.VenueManagementUseCase;
import com.orionticket.events.domain.model.Venue;
import com.orionticket.events.infrastructure.adapters.in.rest.dto.CreateVenueRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/venues")
@RequiredArgsConstructor
@Tag(name = "Venues", description = "Venue Management Endpoints")
public class VenueManagementController {

    private final VenueManagementUseCase venueManagementUseCase;

    // TODO: Extraer desde el token JWT en el futuro
    private final UUID TEMPORARY_ORGANIZER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @PostMapping
    @Operation(summary = "Create a new venue", description = "Creates a new venue for the organizer")
    public ResponseEntity<Venue> createVenue(@Valid @RequestBody CreateVenueRequest request) {
        Venue venue = venueManagementUseCase.createVenue(
                TEMPORARY_ORGANIZER_ID,
                request.getName(),
                request.getAddress(),
                request.getCapacity()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(venue);
    }

    @GetMapping
    @Operation(summary = "Get my venues", description = "Returns the list of venues for the authenticated organizer")
    public ResponseEntity<List<Venue>> getMyVenues() {
        List<Venue> venues = venueManagementUseCase.getVenuesByOrganizer(TEMPORARY_ORGANIZER_ID);
        return ResponseEntity.ok(venues);
    }
}
