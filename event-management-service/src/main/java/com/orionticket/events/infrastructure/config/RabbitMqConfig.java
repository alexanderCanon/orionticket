package com.orionticket.events.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE_NAME = "orionticket.events.exchange";
    public static final String EVENT_CREATED_ROUTING_KEY = "event.created";
    public static final String DATE_ADDED_ROUTING_KEY = "event.date.added";
    public static final String VENUE_CREATED_ROUTING_KEY = "venue.created";
    public static final String EVENT_SUBMITTED_ROUTING_KEY = "event.submitted";
    public static final String EVENT_RELEASED_ROUTING_KEY = "event.released";
    public static final String EVENT_REJECTED_ROUTING_KEY = "event.rejected";

    @Bean
    public DirectExchange eventsExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
