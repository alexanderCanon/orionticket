package com.orionticket.events.infrastructure.adapters.in.rest;

import com.orionticket.events.application.port.in.VenueManagementUseCase;
import com.orionticket.events.domain.model.Venue;
import com.orionticket.events.infrastructure.adapters.in.rest.dto.CreateVenueRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/venues")
@RequiredArgsConstructor
public class VenueManagementController {

    private final VenueManagementUseCase venueManagementUseCase;

    // TODO: Extraer desde el token JWT en el futuro
    private final UUID TEMPORARY_ORGANIZER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @PostMapping
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
    public ResponseEntity<List<Venue>> getMyVenues() {
        List<Venue> venues = venueManagementUseCase.getVenuesByOrganizer(TEMPORARY_ORGANIZER_ID);
        return ResponseEntity.ok(venues);
    }
}
