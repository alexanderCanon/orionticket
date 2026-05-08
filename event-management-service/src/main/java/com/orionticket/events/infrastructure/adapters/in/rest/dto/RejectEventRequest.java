package com.orionticket.events.infrastructure.adapters.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectEventRequest {
    @NotBlank(message = "Reason must not be blank")
    private String reason;
}
