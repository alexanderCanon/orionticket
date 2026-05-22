package com.orionticket.seating.reservation.infrastructure.adapters.in.rest;

import com.orionticket.seating.reservation.application.port.in.ReservationUseCase;
import com.orionticket.seating.reservation.domain.model.Reservation;
import com.orionticket.seating.reservation.infrastructure.adapters.in.rest.dto.CreateReservationRequest;
import com.orionticket.seating.reservation.infrastructure.adapters.in.rest.dto.ReservationResponse;
import com.orionticket.seating.shared.infrastructure.security.AuthenticatedUserResolver;
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

import java.util.UUID;

@RestController
@RequestMapping("/v1/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservations", description = "Seat reservation and release endpoints")
public class ReservationController {

    private final ReservationUseCase reservationUseCase;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    // POST /v1/reservations: el endpoint más crítico del servicio.
    // Delega al ReservationService que maneja el lock pesimista y la transacción atómica.
    // Si el asiento ya está tomado → 409. Si el batch está exhausto → 410.
    @Operation(summary = "Create reservation", description = "Atomically reserves a seat for a buyer using inventory locking.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reservation created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Seat is already reserved or sold"),
            @ApiResponse(responseCode = "410", description = "Batch is exhausted")
    })
    @PostMapping
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody CreateReservationRequest request) {

        Reservation reservation = reservationUseCase.createReservation(
                request.getSeatId(), authenticatedUserResolver.currentUserId(),
                request.getEventId(), request.getDateId(), request.getBatchId());

        return ResponseEntity.status(HttpStatus.CREATED).body(ReservationResponse.from(reservation));
    }

    // DELETE /v1/reservations/{id}: libera la reserva manualmente (operador o sistema).
    // Seat vuelve a AVAILABLE, batch.sold se decrementa, se publica ReservationReleased.
    @Operation(summary = "Release reservation", description = "Releases an existing reservation and returns inventory to availability.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation released"),
            @ApiResponse(responseCode = "404", description = "Reservation not found"),
            @ApiResponse(responseCode = "409", description = "Reservation cannot be released from its current state")
    })
    @DeleteMapping("/{reservationId}")
    @PreAuthorize("hasRole('PLATFORM_OPERATOR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ReservationResponse> releaseReservation(
            @PathVariable UUID reservationId) {

        Reservation reservation = reservationUseCase.releaseReservation(reservationId);
        return ResponseEntity.ok(ReservationResponse.from(reservation));
    }
}
