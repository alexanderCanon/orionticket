package com.orionticket.events.infrastructure.adapters.in.rest.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateVenueRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String address;

    @NotNull
    @Min(1)
    private Integer capacity;
}
