package com.orionticket.events.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
public class EventDate {
    private UUID dateId;
    private UUID eventId;
    private ZonedDateTime scheduledAt;
    private UUID venueId;
    private Integer capacity;
    private ZonedDateTime createdAt;
}
