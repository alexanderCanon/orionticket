package com.orionticket.events.infrastructure.adapters.out.messaging;

import com.orionticket.events.domain.model.Event;
import com.orionticket.events.domain.model.EventDate;
import com.orionticket.events.domain.port.out.EventPublisherPort;
import com.orionticket.events.infrastructure.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMqEventPublisherAdapter implements EventPublisherPort {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishEventCreated(Event event) {
        Map<String, Object> message = new HashMap<>();
        message.put("eventId", event.getEventId());
        message.put("organizerId", event.getOrganizerId());
        message.put("name", event.getName());
        message.put("status", event.getStatus());
        
        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE_NAME, RabbitMqConfig.EVENT_CREATED_ROUTING_KEY, message);
        log.info("Published EventCreated for eventId: {}", event.getEventId());
    }

    @Override
    public void publishDateAdded(Event event, EventDate date) {
        Map<String, Object> message = new HashMap<>();
        message.put("eventId", event.getEventId());
        message.put("dateId", date.getDateId());
        message.put("venueId", date.getVenueId());
        message.put("scheduledAt", date.getScheduledAt().toString());
        message.put("capacity", date.getCapacity());

        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE_NAME, RabbitMqConfig.DATE_ADDED_ROUTING_KEY, message);
        log.info("Published DateAdded for eventId: {}, dateId: {}", event.getEventId(), date.getDateId());
    }
}
