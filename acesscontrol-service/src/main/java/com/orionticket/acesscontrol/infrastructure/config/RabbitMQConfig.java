package com.orionticket.acesscontrol.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "orionticket.access-control.events";

    public static final String VALIDATION_ATTEMPTED_QUEUE = "access-control.validation.attempted";
    public static final String VALIDATION_SUCCEEDED_QUEUE = "access-control.validation.succeeded";
    public static final String VALIDATION_FAILED_QUEUE = "access-control.validation.failed";
    public static final String CONFLICT_DETECTED_QUEUE = "access-control.conflict.detected";
    public static final String VALIDATOR_SYNCED_QUEUE = "access-control.validator.synced";

    @Bean
    public TopicExchange accessControlExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue validationAttemptedQueue() {
        return new Queue(VALIDATION_ATTEMPTED_QUEUE, true);
    }

    @Bean
    public Queue validationSucceededQueue() {
        return new Queue(VALIDATION_SUCCEEDED_QUEUE, true);
    }

    @Bean
    public Queue validationFailedQueue() {
        return new Queue(VALIDATION_FAILED_QUEUE, true);
    }

    @Bean
    public Queue conflictDetectedQueue() {
        return new Queue(CONFLICT_DETECTED_QUEUE, true);
    }

    @Bean
    public Queue validatorSyncedQueue() {
        return new Queue(VALIDATOR_SYNCED_QUEUE, true);
    }

    @Bean
    public Binding validationAttemptedBinding(Queue validationAttemptedQueue, TopicExchange accessControlExchange) {
        return BindingBuilder.bind(validationAttemptedQueue).to(accessControlExchange).with("validation.attempted");
    }

    @Bean
    public Binding validationSucceededBinding(Queue validationSucceededQueue, TopicExchange accessControlExchange) {
        return BindingBuilder.bind(validationSucceededQueue).to(accessControlExchange).with("validation.succeeded");
    }

    @Bean
    public Binding validationFailedBinding(Queue validationFailedQueue, TopicExchange accessControlExchange) {
        return BindingBuilder.bind(validationFailedQueue).to(accessControlExchange).with("validation.failed");
    }

    @Bean
    public Binding conflictDetectedBinding(Queue conflictDetectedQueue, TopicExchange accessControlExchange) {
        return BindingBuilder.bind(conflictDetectedQueue).to(accessControlExchange).with("conflict.detected");
    }

    @Bean
    public Binding validatorSyncedBinding(Queue validatorSyncedQueue, TopicExchange accessControlExchange) {
        return BindingBuilder.bind(validatorSyncedQueue).to(accessControlExchange).with("validator.synced");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
}