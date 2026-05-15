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

    public RabbitMqPaymentEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publishPaymentInitiated(PaymentEvent event) {
        log.info("publishPaymentInitiated — paymentId={} orderId={}", event.paymentId(), event.orderId());
        throw new UnsupportedOperationException("RabbitMQ publishing not wired yet — configure exchange/queue first");
    }

    @Override
    public void publishPaymentAuthorized(PaymentEvent event) {
        log.info("publishPaymentAuthorized — paymentId={} orderId={}", event.paymentId(), event.orderId());
        throw new UnsupportedOperationException("RabbitMQ publishing not wired yet — configure exchange/queue first");
    }

    @Override
    public void publishPaymentFailed(PaymentFailedEvent event) {
        log.info("publishPaymentFailed — paymentId={} orderId={} reason={}", event.paymentId(), event.orderId(), event.failureReason());
        throw new UnsupportedOperationException("RabbitMQ publishing not wired yet — configure exchange/queue first");
    }

    @Override
    public void publishPayoutGenerated(PayoutEvent event) {
        log.info("publishPayoutGenerated — payoutId={} organizerId={}", event.payoutId(), event.organizerId());
        throw new UnsupportedOperationException("RabbitMQ publishing not wired yet — configure exchange/queue first");
    }

    @Override
    public void publishPayoutProcessed(PayoutEvent event) {
        log.info("publishPayoutProcessed — payoutId={} organizerId={}", event.payoutId(), event.organizerId());
        throw new UnsupportedOperationException("RabbitMQ publishing not wired yet — configure exchange/queue first");
    }
}
