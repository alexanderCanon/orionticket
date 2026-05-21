package com.orionticket.payments.infrastructure.adapters.out.messaging;

import com.orionticket.payments.application.port.out.PaymentEventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ adapter for publishing payment domain events.
 * Exchange names and routing keys must be configured in RabbitMqConfig.
 * Event schemas defined in docs/phases/phase-3/event-schemas.md.
 */
@Component
public class RabbitMqPaymentEventPublisher implements PaymentEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(RabbitMqPaymentEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;

    public RabbitMqPaymentEventPublisher(RabbitTemplate rabbitTemplate, 
                                         @org.springframework.beans.factory.annotation.Value("${rabbitmq.exchanges.payments:payments.events}") String exchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
    }

    @Override
    public void publishPaymentInitiated(PaymentEvent event) {
        log.info("publishPaymentInitiated — paymentId={} orderId={}", event.paymentId(), event.orderId());
        rabbitTemplate.convertAndSend(exchange, "payment.initiated", wrapInEnvelope("PaymentInitiated", event));
    }

    @Override
    public void publishPaymentAuthorized(PaymentEvent event) {
        log.info("publishPaymentAuthorized — paymentId={} orderId={}", event.paymentId(), event.orderId());
        rabbitTemplate.convertAndSend(exchange, "payment.authorized", wrapInEnvelope("PaymentAuthorized", event));
    }

    @Override
    public void publishPaymentFailed(PaymentFailedEvent event) {
        log.info("publishPaymentFailed — paymentId={} orderId={} reason={}", event.paymentId(), event.orderId(), event.failureReason());
        rabbitTemplate.convertAndSend(exchange, "payment.failed", wrapInEnvelope("PaymentFailed", event));
    }

    @Override
    public void publishPayoutGenerated(PayoutEvent event) {
        log.info("publishPayoutGenerated — payoutId={} organizerId={}", event.payoutId(), event.organizerId());
        rabbitTemplate.convertAndSend(exchange, "payout.generated", wrapInEnvelope("PayoutGenerated", event));
    }

    @Override
    public void publishPayoutProcessed(PayoutEvent event) {
        log.info("publishPayoutProcessed — payoutId={} organizerId={}", event.payoutId(), event.organizerId());
        rabbitTemplate.convertAndSend(exchange, "payout.processed", wrapInEnvelope("PayoutProcessed", event));
    }

    private java.util.Map<String, Object> wrapInEnvelope(String eventType, Object payload) {
        java.util.Map<String, Object> envelope = new java.util.LinkedHashMap<>();
        envelope.put("eventType", eventType);
        envelope.put("eventId", java.util.UUID.randomUUID().toString());
        envelope.put("occurredAt", java.time.Instant.now().toString());
        envelope.put("payload", payload);
        return envelope;
    }
}
