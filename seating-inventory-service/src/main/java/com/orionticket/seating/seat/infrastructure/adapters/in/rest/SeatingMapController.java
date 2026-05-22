package com.orionticket.seating.seat.infrastructure.adapters.in.rest;

import com.orionticket.seating.seat.application.port.in.SeatingMapUseCase;
import com.orionticket.seating.seat.domain.model.Seat;
import com.orionticket.seating.seat.infrastructure.adapters.in.rest.dto.CreateSeatingMapRequest;
import com.orionticket.seating.seat.infrastructure.adapters.in.rest.dto.SeatResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/events/{eventId}/dates/{dateId}/seating-map")
@RequiredArgsConstructor
@Tag(name = "Seating Map", description = "Mapped seating and general admission configuration endpoints")
public class SeatingMapController {

    private final SeatingMapUseCase seatingMapUseCase;

    @Operation(summary = "Configure seating map", description = "Configures mapped seats or general admission inventory for an event date.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Seating map configured"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Seating map cannot be changed in the current event state")
    })
    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<SeatResponse>> configureSeatingMap(
            @PathVariable UUID eventId,
            @PathVariable UUID dateId,
            @Valid @RequestBody CreateSeatingMapRequest request) {

        List<Seat> seats;

        if (request.getGeneralAdmission() != null) {
            CreateSeatingMapRequest.GeneralAdmissionConfig ga = request.getGeneralAdmission();
            seats = seatingMapUseCase.configureGeneralAdmission(
                    eventId, dateId, request.getBatchId(),
                    ga.getCapacity(), ga.getAccessPolicy());
        } else {
            // Convertir el request jerárquico (zona → sección → fila → asiento)
            // en una lista plana de SeatDefinition para el use case.
            List<SeatingMapUseCase.SeatDefinition> definitions = new ArrayList<>();
            for (CreateSeatingMapRequest.SectionConfig section : request.getMappedSections()) {
                for (CreateSeatingMapRequest.RowConfig row : section.getRows()) {
                    for (String seatNumber : row.getSeatNumbers()) {
                        definitions.add(new SeatingMapUseCase.SeatDefinition(
                                section.getZone(), section.getSection(),
                                row.getRowLabel(), seatNumber,
                                section.getAccessPolicy()));
                    }
                }
            }
            seats = seatingMapUseCase.configureMappedSeats(eventId, dateId, request.getBatchId(), definitions);
        }

        List<SeatResponse> response = seats.stream().map(SeatResponse::from).collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
