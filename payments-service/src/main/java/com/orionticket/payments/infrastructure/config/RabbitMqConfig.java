package com.orionticket.payments.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ topology for the Payments service.
 *
 * Produces to:
 *   - payments.events  (TopicExchange) with routing keys:
 *       payment.initiated, payment.authorized, payment.failed,
 *       payout.generated, payout.processed
 *
 * Consumes from:
 *   - orders.events       → routing key: order.created
 *   - event-mgmt.events   → routing key: date.added
 *
 * Each consumer queue has a corresponding DLQ for unprocessable messages.
 */
@Configuration
public class RabbitMqConfig {

    // -------------------------------------------------------------------------
    // Exchange names (injected from application.yml for environment flexibility)
    // -------------------------------------------------------------------------

    @Value("${rabbitmq.exchanges.payments:payments.events}")
    private String paymentsExchange;

    @Value("${rabbitmq.exchanges.orders:orders.events}")
    private String ordersExchange;

    @Value("${rabbitmq.exchanges.event-management:event-mgmt.events}")
    private String eventMgmtExchange;

    // -------------------------------------------------------------------------
    // Queue names
    // -------------------------------------------------------------------------

    @Value("${rabbitmq.queues.order-created:payments.queue.order-created}")
    private String orderCreatedQueue;

    @Value("${rabbitmq.queues.date-added:payments.queue.date-added}")
    private String dateAddedQueue;

    // -------------------------------------------------------------------------
    // Message converter
    // -------------------------------------------------------------------------

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    // -------------------------------------------------------------------------
    // Exchanges
    // -------------------------------------------------------------------------

    /** Exchange where Payments publishes its own events. */
    @Bean
    public TopicExchange paymentsTopicExchange() {
        return ExchangeBuilder.topicExchange(paymentsExchange).durable(true).build();
    }

    /** Exchange owned by Orders service — Payments subscribes to it. */
    @Bean
    public TopicExchange ordersTopicExchange() {
        return ExchangeBuilder.topicExchange(ordersExchange).durable(true).build();
    }

    /** Exchange owned by Event Management service — Payments subscribes to it. */
    @Bean
    public TopicExchange eventMgmtTopicExchange() {
        return ExchangeBuilder.topicExchange(eventMgmtExchange).durable(true).build();
    }

    // -------------------------------------------------------------------------
    // Dead-letter exchanges
    // -------------------------------------------------------------------------

    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange("payments.dlx").durable(true).build();
    }

    // -------------------------------------------------------------------------
    // Consumer queues (with DLQ routing)
    // -------------------------------------------------------------------------

    @Bean
    public Queue orderCreatedQueue() {
        return QueueBuilder.durable(orderCreatedQueue)
                .withArgument("x-dead-letter-exchange", "payments.dlx")
                .withArgument("x-dead-letter-routing-key", orderCreatedQueue + ".dlq")
                .build();
    }

    @Bean
    public Queue orderCreatedDlq() {
        return QueueBuilder.durable(orderCreatedQueue + ".dlq").build();
    }

    @Bean
    public Queue dateAddedQueue() {
        return QueueBuilder.durable(dateAddedQueue)
                .withArgument("x-dead-letter-exchange", "payments.dlx")
                .withArgument("x-dead-letter-routing-key", dateAddedQueue + ".dlq")
                .build();
    }

    @Bean
    public Queue dateAddedDlq() {
        return QueueBuilder.durable(dateAddedQueue + ".dlq").build();
    }

    // -------------------------------------------------------------------------
    // Bindings — subscribe Payments queues to other services' exchanges
    // -------------------------------------------------------------------------

    @Bean
    public Binding orderCreatedBinding(Queue orderCreatedQueue, TopicExchange ordersTopicExchange) {
        return BindingBuilder.bind(orderCreatedQueue).to(ordersTopicExchange).with("order.created");
    }

    @Bean
    public Binding dateAddedBinding(Queue dateAddedQueue, TopicExchange eventMgmtTopicExchange) {
        return BindingBuilder.bind(dateAddedQueue).to(eventMgmtTopicExchange).with("date.added");
    }

    @Bean
    public Binding orderCreatedDlqBinding(Queue orderCreatedDlq) {
        return BindingBuilder.bind(orderCreatedDlq).to(deadLetterExchange())
                .with(orderCreatedQueue + ".dlq");
    }

    @Bean
    public Binding dateAddedDlqBinding(Queue dateAddedDlq) {
        return BindingBuilder.bind(dateAddedDlq).to(deadLetterExchange())
                .with(dateAddedQueue + ".dlq");
    }
}