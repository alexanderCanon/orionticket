package com.orionticket.ticketissuance.infrastructure.adapters.out.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orionticket.ticketissuance.application.port.out.TicketEventPublisherPort;
import com.orionticket.ticketissuance.domain.model.Ticket;
import com.orionticket.ticketissuance.infrastructure.config.RabbitMqConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RabbitMQEventPublisherAdapter implements TicketEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQEventPublisherAdapter.class);
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public RabbitMQEventPublisherAdapter(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publishTicketIssuedEvent(Ticket ticket) {
        try {
            // Map domain object to the format expected by notifications-service
            Map<String, Object> event = new HashMap<>();
            event.put("recipientId", ticket.buyerId().toString());
            event.put("channel", ticket.deliveryChannels().iterator().next().name()); // Use first selected channel
            event.put("templateId", "ticket-confirmation");
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("userName", ticket.holderName());
            payload.put("ticketId", ticket.ticketId().toString());
            payload.put("eventId", ticket.eventId().toString());
            
            event.put("payload", payload);
            event.put("eventType", "TicketIssued");

            String message = objectMapper.writeValueAsString(event);
            
            log.info("Publishing TicketIssued event to RabbitMQ for ticket: {}", ticket.ticketId());
            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.NOTIFICATION_EVENTS_EXCHANGE,
                    RabbitMqConfig.TICKET_ISSUED_ROUTING_KEY,
                    message
            );
            
        } catch (Exception e) {
            log.error("Error publishing TicketIssued event for ticket: {}", ticket.ticketId(), e);
        }
    }
}
