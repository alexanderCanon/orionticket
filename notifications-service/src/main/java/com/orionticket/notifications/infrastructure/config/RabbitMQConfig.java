package com.orionticket.notifications.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String NOTIFICATION_EVENTS_EXCHANGE = "notification-events";
    public static final String NOTIFICATION_EVENTS_QUEUE = "notification-events";
    public static final String TICKET_ISSUED_ROUTING_KEY = "ticket.issued";

    @Bean
    public TopicExchange notificationEventsExchange() {
        return new TopicExchange(NOTIFICATION_EVENTS_EXCHANGE, true, false);
    }

    @Bean
    public Queue notificationEventsQueue() {
        return new Queue(NOTIFICATION_EVENTS_QUEUE, true);
    }

    @Bean
    public Binding ticketIssuedBinding(Queue notificationEventsQueue, TopicExchange notificationEventsExchange) {
        return BindingBuilder.bind(notificationEventsQueue)
                .to(notificationEventsExchange)
                .with(TICKET_ISSUED_ROUTING_KEY);
    }
}
