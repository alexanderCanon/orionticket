package com.orionticket.events.infrastructure.adapters.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateEventRequest {
    @NotBlank
    private String name;
    
    private String description;
}
