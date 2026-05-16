package com.orionticket.orders.order.infrastructure.adapters.in.rest.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateOrderRequest {

    @NotNull
    private UUID buyerId;

    @NotNull
    private UUID eventId;

    @NotNull
    private UUID dateId;

    @NotNull
    private UUID reservationId;

    private String promotionCode;   // opcional
}
