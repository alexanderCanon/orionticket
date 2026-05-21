package com.orionticket.orders.shared.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

@Configuration
public class RabbitMqConfig {

    // Exchange compartido con todos los servicios (Topic permite routing por patrón)
    public static final String EXCHANGE = "orionticket.events.exchange";

    // DLX/DLQ: mensajes que fallan reiteradamente van aquí en lugar de perderse
    public static final String DLX = "orionticket.dlx";
    public static final String DLQ = "orionticket.dlq";

    // Colas que este servicio consume
    public static final String QUEUE_RESERVATION_CREATED = "orders.reservation.created.queue";
    public static final String QUEUE_RESERVATION_EXPIRED = "orders.reservation.expired.queue";
    public static final String QUEUE_PAYMENT_AUTHORIZED  = "orders.payment.authorized.queue";

    // Routing keys de eventos entrantes
    public static final String RK_RESERVATION_CREATED = "reservation.created";
    public static final String RK_RESERVATION_EXPIRED = "reservation.expired";
    public static final String RK_PAYMENT_AUTHORIZED  = "payment.authorized";

    // Routing keys de eventos que este servicio publica
    public static final String RK_ORDER_CREATED      = "order.created";
    public static final String RK_ORDER_EXPIRED      = "order.expired";
    public static final String RK_ORDER_CONFIRMED    = "order.confirmed";
    public static final String RK_PROMOTION_EXHAUSTED = "promotion.exhausted";

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX, true, false);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ).build();
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with(DLQ);
    }

    // Cada cola de consumo redirige mensajes fallidos al DLX
    @Bean
    public Queue reservationCreatedQueue() {
        return QueueBuilder.durable(QUEUE_RESERVATION_CREATED)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", DLQ)
                .build();
    }

    @Bean
    public Queue reservationExpiredQueue() {
        return QueueBuilder.durable(QUEUE_RESERVATION_EXPIRED)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", DLQ)
                .build();
    }

    @Bean
    public Queue paymentAuthorizedQueue() {
        return QueueBuilder.durable(QUEUE_PAYMENT_AUTHORIZED)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", DLQ)
                .build();
    }

    @Bean
    public Binding reservationCreatedBinding() {
        return BindingBuilder.bind(reservationCreatedQueue()).to(topicExchange()).with(RK_RESERVATION_CREATED);
    }

    @Bean
    public Binding reservationExpiredBinding() {
        return BindingBuilder.bind(reservationExpiredQueue()).to(topicExchange()).with(RK_RESERVATION_EXPIRED);
    }

    @Bean
    public Binding paymentAuthorizedBinding() {
        return BindingBuilder.bind(paymentAuthorizedQueue()).to(topicExchange()).with(RK_PAYMENT_AUTHORIZED);
    }

    // Convierte mensajes a/de JSON automáticamente
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // RabbitTemplate usa el converter JSON para publicar eventos
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    // Política de reintentos para consumers: 3 intentos con backoff exponencial
    @Bean
    public RetryOperationsInterceptor retryInterceptor() {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .backOffOptions(1000, 2.0, 10000)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build();
    }

    // Factory de listener containers con reintentos y conversión JSON
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setAdviceChain(retryInterceptor());
        return factory;
    }
}
