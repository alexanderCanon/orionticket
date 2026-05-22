package com.orionticket.events.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * Configuración de RabbitMQ para el Event Management Service.
 * <p>
 * Implementa el patrón Dead Letter Exchange (DLX) requerido por la DoD §4:
 * - Máximo 3 reintentos con backoff exponencial (1s → 2s → 4s).
 * - Mensajes fallidos tras 3 intentos → DLQ (Dead Letter Queue).
 * - Conversión de mensajes a JSON con Jackson.
 * </p>
 */
@Configuration
public class RabbitMqConfig {

    // ── Exchange y routing keys de dominio ──────────────────────────────────
    public static final String EXCHANGE_NAME              = "orionticket.events.exchange";
    public static final String EVENT_CREATED_ROUTING_KEY = "event.created";
    public static final String DATE_ADDED_ROUTING_KEY    = "event.date.added";
    public static final String VENUE_CREATED_ROUTING_KEY = "venue.created";
    public static final String EVENT_SUBMITTED_ROUTING_KEY = "event.submitted";
    public static final String EVENT_RELEASED_ROUTING_KEY  = "event.released";
    public static final String EVENT_REJECTED_ROUTING_KEY  = "event.rejected";
    public static final String EVENT_CANCELED_ROUTING_KEY  = "event.canceled";
    public static final String DATE_CANCELED_ROUTING_KEY   = "event.date.canceled";

    // ── Dead Letter Exchange ─────────────────────────────────────────────────
    public static final String DLX_EXCHANGE_NAME = "orionticket.events.dlx";
    public static final String DLQ_NAME          = "orionticket.events.dlq";

    // ── Exchange principal ───────────────────────────────────────────────────

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    // ── Dead Letter Exchange + Queue ─────────────────────────────────────────

    /**
     * Exchange de dead-letter. Mensajes fallidos se enrutan aquí.
     */
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX_EXCHANGE_NAME, true, false);
    }

    /**
     * Cola de dead-letter. Recibe mensajes que fallaron tras todos los reintentos.
     */
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ_NAME).build();
    }

    /**
     * Binding: DLX → DLQ con routing key "#" (captura cualquier routing key fallida).
     */
    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with("#");
    }

    // ── Conversión de mensajes ───────────────────────────────────────────────

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    // ── Retry con backoff exponencial ────────────────────────────────────────

    /**
     * Política de reintentos: máximo 3 intentos, backoff exponencial 1s→2s→4s.
     * Tras agotar reintentos, el {@link RejectAndDontRequeueRecoverer} enruta
     * el mensaje al DLX configurado en la queue (x-dead-letter-exchange).
     */
    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        ExponentialBackOffPolicy backOff = new ExponentialBackOffPolicy();
        backOff.setInitialInterval(1000L);
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(10000L);
        retryTemplate.setBackOffPolicy(backOff);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }

    /**
     * Recoverer: tras agotar los reintentos, rechaza el mensaje para que
     * RabbitMQ lo enrute al DLX asociado a la queue.
     */
    @Bean
    public MessageRecoverer messageRecoverer() {
        return new RejectAndDontRequeueRecoverer();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}
