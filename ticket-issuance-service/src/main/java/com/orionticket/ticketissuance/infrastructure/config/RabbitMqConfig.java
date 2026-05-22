package com.orionticket.ticketissuance.infrastructure.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String NOTIFICATION_EVENTS_EXCHANGE = "notification-events";
    public static final String TICKET_ISSUED_ROUTING_KEY = "ticket.issued";

    @Bean
    public TopicExchange notificationEventsExchange() {
        return new TopicExchange(NOTIFICATION_EVENTS_EXCHANGE, true, false);
    }
}
