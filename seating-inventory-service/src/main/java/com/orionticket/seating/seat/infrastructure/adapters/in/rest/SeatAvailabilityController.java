package com.orionticket.seating.seat.infrastructure.adapters.in.rest;

import com.orionticket.seating.seat.application.port.in.SeatAvailabilityUseCase;
import com.orionticket.seating.seat.infrastructure.adapters.in.rest.dto.SeatResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

// Público: SecurityConfig permite /v1/events/**/seats sin JWT.
// Un comprador anónimo puede ver la disponibilidad antes de loguearse.
@RestController
@RequestMapping("/v1/events/{eventId}/dates/{dateId}/seats")
@RequiredArgsConstructor
@Tag(name = "Seat Availability", description = "Public seat availability endpoints")
public class SeatAvailabilityController {

    private final SeatAvailabilityUseCase seatAvailabilityUseCase;

    @Operation(summary = "List available seats", description = "Returns available seats for an event date with optional zone and section filters.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Available seats returned")
    })
    @GetMapping
    public ResponseEntity<List<SeatResponse>> getSeats(
            @PathVariable UUID eventId,
            @PathVariable UUID dateId,
            @RequestParam(required = false) String zone,
            @RequestParam(required = false) String section) {

        List<SeatResponse> seats = seatAvailabilityUseCase
                .getAvailableSeats(eventId, dateId, zone, section)
                .stream().map(SeatResponse::from).collect(Collectors.toList());

        return ResponseEntity.ok(seats);
    }
}
