package com.orionticket.events.infrastructure.adapters.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO para la solicitud de cancelación de un evento.
 * <p>
 * El campo {@code reason} es obligatorio según el contrato de API definido
 * en {@code docs/phases/phase-3/service-contracts.md} para el endpoint
 * {@code POST /v1/events/{eventId}/cancel}.
 * </p>
 */
@Data
public class CancelEventRequest {

    @NotBlank(message = "Reason must not be blank")
    private String reason;
}
