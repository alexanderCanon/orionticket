package com.orionticket.seating.seat.infrastructure.adapters.in.rest;

import com.orionticket.seating.seat.application.port.in.SeatAvailabilityUseCase;
import com.orionticket.seating.seat.infrastructure.adapters.in.rest.dto.SeatResponse;
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
public class SeatAvailabilityController {

    private final SeatAvailabilityUseCase seatAvailabilityUseCase;

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
