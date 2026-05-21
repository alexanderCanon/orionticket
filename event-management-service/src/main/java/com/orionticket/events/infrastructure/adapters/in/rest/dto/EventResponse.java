package com.orionticket.events.infrastructure.adapters.in.rest.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class EventResponse {
    private UUID eventId;
    private String name;
    private String category;
    private String description;
    private String organizerName;
    private List<EventDateResponse> dates;
}
