package com.orionticket.events.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class Event {
    private UUID eventId;
    private UUID organizerId;
    private String name;
    private String description;
    private String status;
    @Builder.Default
    private List<EventDate> dates = new ArrayList<>();
    private ZonedDateTime createdAt;

    /**
     * Regla de Negocio: Un nuevo evento siempre inicia como DRAFT.
     */
    public static Event createDraft(UUID organizerId, String name, String description) {
        return Event.builder()
                .eventId(UUID.randomUUID())
                .organizerId(organizerId)
                .name(name)
                .description(description)
                .status("DRAFT")
                .createdAt(ZonedDateTime.now())
                .build();
    }

    /**
     * Regla de Negocio: Agregar una fecha a un evento.
     */
    public EventDate addDate(ZonedDateTime scheduledAt, UUID venueId, Integer capacity) {
        EventDate newDate = EventDate.builder()
                .dateId(UUID.randomUUID())
                .eventId(this.eventId)
                .scheduledAt(scheduledAt)
                .venueId(venueId)
                .capacity(capacity)
                .createdAt(ZonedDateTime.now())
                .build();
        
        this.dates.add(newDate);
        return newDate;
    }
}
