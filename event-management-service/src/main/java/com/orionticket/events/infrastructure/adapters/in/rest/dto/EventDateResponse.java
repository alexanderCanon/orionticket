package com.orionticket.events.infrastructure.adapters.in.rest.dto;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
public class EventDateResponse {
    private UUID dateId;
    private ZonedDateTime scheduledAt;
    private String venueName;
    private Integer availableSeats;
}
