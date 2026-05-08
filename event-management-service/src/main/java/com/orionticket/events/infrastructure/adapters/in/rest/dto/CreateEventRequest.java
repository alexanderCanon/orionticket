package com.orionticket.events.infrastructure.adapters.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO de solicitud para la creación de un evento.
 * Todos los campos obligatorios están validados con Bean Validation.
 */
@Data
public class CreateEventRequest {

    @NotBlank(message = "El nombre del evento es obligatorio")
    private String name;

    private String description;

    @NotBlank(message = "La categoría del evento es obligatoria")
    private String category;
}
