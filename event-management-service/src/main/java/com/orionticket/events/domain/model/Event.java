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
    private String category;
    private String status;
    private String rejectionReason;
    @Builder.Default
    private List<EventDate> dates = new ArrayList<>();
    private ZonedDateTime createdAt;

    /**
     * Regla de Negocio: Un nuevo evento siempre inicia como DRAFT.
     */
    public static Event createDraft(UUID organizerId, String name, String description, String category) {
        return Event.builder()
                .eventId(UUID.randomUUID())
                .organizerId(organizerId)
                .name(name)
                .description(description)
                .category(category)
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

    /**
     * Regla de Negocio: Enviar el evento a revisión.
     * Requiere que el evento tenga al menos una fecha configurada.
     */
    public void submitForReview() {
        if (!"DRAFT".equals(this.status)) {
            throw new com.orionticket.events.domain.exception.InvalidEventStateException("Only events in DRAFT status can be submitted for review.");
        }
        if (this.dates == null || this.dates.isEmpty()) {
            throw new com.orionticket.events.domain.exception.InvalidEventStateException("Cannot submit an event without dates.");
        }
        
        this.status = "UNDER_REVIEW";
    }

    /**
     * Regla de Negocio: Aprobar el evento (Solo por Platform Operator).
     */
    public void approve() {
        if (!"UNDER_REVIEW".equals(this.status)) {
            throw new com.orionticket.events.domain.exception.InvalidEventStateException("Only events UNDER_REVIEW can be approved.");
        }
        this.status = "RELEASED";
    }

    /**
     * Regla de Negocio: Rechazar el evento (Solo por Platform Operator).
     */
    public void reject(String reason) {
        if (!"UNDER_REVIEW".equals(this.status)) {
            throw new com.orionticket.events.domain.exception.InvalidEventStateException("Only events UNDER_REVIEW can be rejected.");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("A rejection reason must be provided.");
        }
        this.status = "DRAFT";
        this.rejectionReason = reason;
    }

    /**
     * Regla de Negocio: Cancelar el evento.
     */
    public void cancel() {
        if ("CANCELED".equals(this.status)) {
            throw new com.orionticket.events.domain.exception.InvalidEventStateException("Event is already canceled.");
        }
        this.status = "CANCELED";
    }
}
