package com.orionticket.payments.infrastructure.adapters.out.messaging;

import com.orionticket.payments.application.port.out.PaymentEventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * RabbitMQ adapter for publishing payment domain events.
 * Exchange names and routing keys configured in RabbitMqConfig.
 * Event schemas defined in docs/phases/phase-3/event-schemas.md.
 */
@Component
public class RabbitMqPaymentEventPublisher implements PaymentEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(RabbitMqPaymentEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final String paymentsExchange;

    public RabbitMqPaymentEventPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${rabbitmq.exchanges.payments:payments.events}") String paymentsExchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.paymentsExchange = paymentsExchange;
    }

    private void publish(String eventType, Map<String, Object> payload, String routingKey) {
        Map<String, Object> message = new HashMap<>();
        message.put("eventType", eventType);
        message.put("eventId", UUID.randomUUID().toString());
        message.put("occurredAt", Instant.now().toString());
        message.put("payload", payload);

        log.info("Publishing event {} with routing key {} to exchange {}", eventType, routingKey, paymentsExchange);
        rabbitTemplate.convertAndSend(paymentsExchange, routingKey, message);
    }

    @Override
    public void publishPaymentInitiated(PaymentEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("paymentId", event.paymentId());
        payload.put("orderId", event.orderId());
        payload.put("buyerId", event.buyerId());
        payload.put("amount", event.amount());
        payload.put("serviceFee", event.serviceFee());
        payload.put("currency", event.currency());
        payload.put("method", event.method());
        payload.put("gatewayReference", event.gatewayReference());
        payload.put("idempotencyKey", event.idempotencyKey());
        payload.put("status", event.status());

        publish("PaymentInitiated", payload, "payment.initiated");
    }

    @Override
    public void publishPaymentAuthorized(PaymentEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("paymentId", event.paymentId());
        payload.put("orderId", event.orderId());
        payload.put("buyerId", event.buyerId());
        payload.put("amount", event.amount());
        payload.put("serviceFee", event.serviceFee());
        payload.put("currency", event.currency());
        payload.put("method", event.method());
        payload.put("gatewayReference", event.gatewayReference());
        payload.put("idempotencyKey", event.idempotencyKey());
        payload.put("status", event.status());

        publish("PaymentAuthorized", payload, "payment.authorized");
    }

    @Override
    public void publishPaymentFailed(PaymentFailedEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("paymentId", event.paymentId());
        payload.put("orderId", event.orderId());
        payload.put("buyerId", event.buyerId());
        payload.put("amount", event.amount());
        payload.put("currency", event.currency());
        payload.put("method", event.method());
        payload.put("failureReason", event.failureReason());
        payload.put("status", event.status());

        publish("PaymentFailed", payload, "payment.failed");
    }

    @Override
    public void publishPayoutGenerated(PayoutEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("payoutId", event.payoutId());
        payload.put("organizerId", event.organizerId());
        payload.put("eventId", event.eventId());
        payload.put("dateId", event.dateId());
        payload.put("grossAmount", event.grossAmount());
        payload.put("serviceFeeTotal", event.serviceFeeTotal());
        payload.put("netAmount", event.netAmount());
        payload.put("status", event.status());

        publish("PayoutGenerated", payload, "payout.generated");
    }

    @Override
    public void publishPayoutProcessed(PayoutEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("payoutId", event.payoutId());
        payload.put("organizerId", event.organizerId());
        payload.put("eventId", event.eventId());
        payload.put("dateId", event.dateId());
        payload.put("grossAmount", event.grossAmount());
        payload.put("serviceFeeTotal", event.serviceFeeTotal());
        payload.put("netAmount", event.netAmount());
        payload.put("status", event.status());

        publish("PayoutProcessed", payload, "payout.processed");
    }
}
