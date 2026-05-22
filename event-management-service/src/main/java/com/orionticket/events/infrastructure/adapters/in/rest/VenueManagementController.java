package com.orionticket.events.infrastructure.adapters.in.rest;

import com.orionticket.events.application.port.in.VenueManagementUseCase;
import com.orionticket.events.domain.model.Venue;
import com.orionticket.events.infrastructure.adapters.in.rest.dto.CreateVenueRequest;
import com.orionticket.events.infrastructure.adapters.out.security.AuthenticatedUserResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/venues")
@RequiredArgsConstructor
@Tag(name = "Venues", description = "Venue Management Endpoints")
public class VenueManagementController {

    private final VenueManagementUseCase venueManagementUseCase;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER')")
    @Operation(summary = "Create a new venue", description = "Creates a new venue for the organizer")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Venue created"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<Venue> createVenue(@Valid @RequestBody CreateVenueRequest request) {
        Venue venue = venueManagementUseCase.createVenue(
                authenticatedUserResolver.requireOrganizerId(),
                request.getName(),
                request.getAddress(),
                request.getCapacity()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(venue);
    }

    @GetMapping
    @PreAuthorize("hasRole('ORGANIZER')")
    @Operation(summary = "Get my venues", description = "Returns the list of venues for the authenticated organizer")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Venues returned")
    })
    public ResponseEntity<List<Venue>> getMyVenues() {
        List<Venue> venues = venueManagementUseCase.getVenuesByOrganizer(authenticatedUserResolver.requireOrganizerId());
        return ResponseEntity.ok(venues);
    }
}
