package com.orionticket.events.infrastructure.adapters.in.rest.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
public class AddEventDateRequest {
    @NotNull
    private ZonedDateTime scheduledAt;
    
    @NotNull
    private UUID venueId;
    
    @NotNull
    @Min(1)
    private Integer capacity;
}
