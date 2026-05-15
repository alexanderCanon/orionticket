package com.orionticket.seating.reservation.infrastructure.adapters.in.rest.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateReservationRequest {

    @NotNull
    private UUID seatId;

    @NotNull
    private UUID buyerId;

    @NotNull
    private UUID eventId;

    @NotNull
    private UUID dateId;

    @NotNull
    private UUID batchId;
}
