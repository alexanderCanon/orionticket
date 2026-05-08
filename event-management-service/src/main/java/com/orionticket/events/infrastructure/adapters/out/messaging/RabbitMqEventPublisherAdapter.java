package com.orionticket.events.infrastructure.adapters.out.messaging;

import com.orionticket.events.domain.model.Event;
import com.orionticket.events.domain.model.EventDate;
import com.orionticket.events.domain.model.Venue;
import com.orionticket.events.domain.port.out.EventPublisherPort;
import com.orionticket.events.infrastructure.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Adaptador de salida que publica eventos de dominio al exchange de RabbitMQ.
 * <p>
 * Todos los mensajes siguen el envelope estándar definido en
 * {@code docs/phases/phase-3/event-schemas.md}:
 * <pre>
 * {
 *   "eventType": "string",
 *   "eventId":   "uuid",       // UUID del mensaje (no del agregado)
 *   "occurredAt": "ISO-8601",
 *   "payload":   { ... }
 * }
 * </pre>
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMqEventPublisherAdapter implements EventPublisherPort {

    private final RabbitTemplate rabbitTemplate;

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Construye el envelope estándar con el payload dado y lo envía al exchange.
     */
    private void publish(String eventType, Map<String, Object> payload, String routingKey) {
        Map<String, Object> message = new HashMap<>();
        message.put("eventType", eventType);
        message.put("eventId", UUID.randomUUID());
        message.put("occurredAt", Instant.now().toString());
        message.put("payload", payload);

        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE_NAME, routingKey, message);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Event lifecycle
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Publica {@code EventCreated} cuando el Organizador crea un nuevo Evento.
     * Schema: event-schemas.md §EventCreated
     */
    @Override
    public void publishEventCreated(Event event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventEntityId", event.getEventId());
        payload.put("organizerId", event.getOrganizerId());
        payload.put("name", event.getName());
        payload.put("category", event.getCategory());
        payload.put("status", event.getStatus());

        publish("EventCreated", payload, RabbitMqConfig.EVENT_CREATED_ROUTING_KEY);
        log.info("Published EventCreated for eventEntityId: {}", event.getEventId());
    }

    /**
     * Publica {@code DateAdded} cuando el Organizador añade una Fecha a un Evento.
     * Schema: event-schemas.md §DateAdded
     */
    @Override
    public void publishDateAdded(Event event, EventDate date) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("dateId", date.getDateId());
        payload.put("eventEntityId", event.getEventId());
        payload.put("scheduledAt", date.getScheduledAt().toString());
        payload.put("venueId", date.getVenueId());
        payload.put("capacity", date.getCapacity());

        publish("DateAdded", payload, RabbitMqConfig.DATE_ADDED_ROUTING_KEY);
        log.info("Published DateAdded for eventEntityId: {}, dateId: {}", event.getEventId(), date.getDateId());
    }

    /**
     * Publica {@code VenueCreated} cuando el Organizador crea un Venue.
     * Schema: event-schemas.md §VenueCreated
     */
    @Override
    public void publishVenueCreated(Venue venue) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("venueId", venue.getVenueId());
        payload.put("organizerId", venue.getOrganizerId());
        payload.put("name", venue.getName());
        payload.put("address", venue.getAddress());
        payload.put("capacity", venue.getCapacity());

        publish("VenueCreated", payload, RabbitMqConfig.VENUE_CREATED_ROUTING_KEY);
        log.info("Published VenueCreated for venueId: {}", venue.getVenueId());
    }

    /**
     * Publica {@code EventSubmittedForReview} cuando el Organizador envía el Evento a revisión.
     * Schema: event-schemas.md §EventSubmittedForReview
     */
    @Override
    public void publishEventSubmittedForReview(Event event, UUID submittedBy) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventEntityId", event.getEventId());
        payload.put("organizerId", event.getOrganizerId());
        payload.put("submittedBy", submittedBy);
        payload.put("status", event.getStatus());

        publish("EventSubmittedForReview", payload, RabbitMqConfig.EVENT_SUBMITTED_ROUTING_KEY);
        log.info("Published EventSubmittedForReview for eventEntityId: {}", event.getEventId());
    }

    /**
     * Publica {@code EventReleased} cuando el Platform Operator aprueba el Evento.
     * Schema: event-schemas.md §EventReleased
     */
    @Override
    public void publishEventReleased(Event event, UUID operatorId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventEntityId", event.getEventId());
        payload.put("organizerId", event.getOrganizerId());
        payload.put("approvedBy", operatorId);
        payload.put("status", event.getStatus());

        publish("EventReleased", payload, RabbitMqConfig.EVENT_RELEASED_ROUTING_KEY);
        log.info("Published EventReleased for eventEntityId: {}", event.getEventId());
    }

    /**
     * Publica {@code EventRejected} cuando el Platform Operator rechaza el Evento.
     * Schema: event-schemas.md §EventRejected
     */
    @Override
    public void publishEventRejected(Event event, UUID operatorId, String reason) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventEntityId", event.getEventId());
        payload.put("organizerId", event.getOrganizerId());
        payload.put("rejectedBy", operatorId);
        payload.put("reason", reason);
        payload.put("status", event.getStatus());

        publish("EventRejected", payload, RabbitMqConfig.EVENT_REJECTED_ROUTING_KEY);
        log.info("Published EventRejected for eventEntityId: {}, reason: {}", event.getEventId(), reason);
    }

    /**
     * Publica {@code EventCanceled} cuando el Organizador cancela el Evento.
     * Schema: event-schemas.md §EventCanceled
     */
    @Override
    public void publishEventCanceled(Event event, UUID canceledBy, String reason) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventEntityId", event.getEventId());
        payload.put("organizerId", event.getOrganizerId());
        payload.put("canceledBy", canceledBy);
        payload.put("reason", reason);

        publish("EventCanceled", payload, RabbitMqConfig.EVENT_CANCELED_ROUTING_KEY);
        log.info("Published EventCanceled for eventEntityId: {}, canceledBy: {}", event.getEventId(), canceledBy);
    }

    /**
     * Publica {@code DateCanceled} por cada Fecha del Evento cancelado.
     * Schema: event-schemas.md §DateCanceled
     */
    @Override
    public void publishDateCanceled(EventDate date, UUID canceledBy, String reason) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("dateId", date.getDateId());
        payload.put("eventEntityId", date.getEventId());
        payload.put("canceledBy", canceledBy);
        payload.put("reason", reason);

        publish("DateCanceled", payload, RabbitMqConfig.DATE_CANCELED_ROUTING_KEY);
        log.info("Published DateCanceled for dateId: {}, canceledBy: {}", date.getDateId(), canceledBy);
    }
}
