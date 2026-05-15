package com.orionticket.seating.reservation.infrastructure.adapters.in.rest;

import com.orionticket.seating.reservation.application.port.in.ReservationUseCase;
import com.orionticket.seating.reservation.domain.model.Reservation;
import com.orionticket.seating.reservation.infrastructure.adapters.in.rest.dto.CreateReservationRequest;
import com.orionticket.seating.reservation.infrastructure.adapters.in.rest.dto.ReservationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationUseCase reservationUseCase;

    // POST /v1/reservations: el endpoint más crítico del servicio.
    // Delega al ReservationService que maneja el lock pesimista y la transacción atómica.
    // Si el asiento ya está tomado → 409. Si el batch está exhausto → 410.
    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody CreateReservationRequest request) {

        Reservation reservation = reservationUseCase.createReservation(
                request.getSeatId(), request.getBuyerId(),
                request.getEventId(), request.getDateId(), request.getBatchId());

        return ResponseEntity.status(HttpStatus.CREATED).body(ReservationResponse.from(reservation));
    }

    // DELETE /v1/reservations/{id}: libera la reserva manualmente (operador o sistema).
    // Seat vuelve a AVAILABLE, batch.sold se decrementa, se publica ReservationReleased.
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<ReservationResponse> releaseReservation(
            @PathVariable UUID reservationId) {

        Reservation reservation = reservationUseCase.releaseReservation(reservationId);
        return ResponseEntity.ok(ReservationResponse.from(reservation));
    }
}
