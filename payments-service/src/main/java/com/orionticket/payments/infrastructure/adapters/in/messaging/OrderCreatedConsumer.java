package com.orionticket.payments.infrastructure.adapters.in.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.orionticket.payments.infrastructure.adapters.out.persistence.entity.OrderProjectionEntity;
import com.orionticket.payments.infrastructure.adapters.out.persistence.repository.SpringDataOrderProjectionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Consumes OrderCreated events from the Orders service.
 * Builds a local order projection to eliminate synchronous HTTP calls
 * to Orders during payment initiation.
 *
 * Event schema: docs/phases/phase-3/event-schemas.md — OrderCreated
 */
@Component
public class OrderCreatedConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedConsumer.class);

    private final SpringDataOrderProjectionRepository repository;

    public OrderCreatedConsumer(SpringDataOrderProjectionRepository repository) {
        this.repository = repository;
    }

    @RabbitListener(queues = "${rabbitmq.queues.order-created:payments.queue.order-created}")
    @Transactional
    public void consume(OrderCreatedMessage message) {
        if (message == null || message.payload() == null) {
            log.warn("Received null or empty OrderCreated message — skipping");
            return;
        }

        OrderCreatedMessage.Payload p = message.payload();
        log.info("Received OrderCreated — orderId={} dateId={}", p.orderId(), p.dateId());

        // Idempotent upsert: if the projection already exists, update status only
        OrderProjectionEntity entity = repository.findById(p.orderId())
                .orElseGet(OrderProjectionEntity::new);

        entity.setOrderId(p.orderId());
        entity.setBuyerId(p.buyerId());
        entity.setEventId(p.eventEntityId());
        entity.setDateId(p.dateId());
        entity.setTotal(p.total());
        entity.setServiceFee(p.serviceFee());
        entity.setCurrency(p.currency());
        entity.setStatus(p.status());
        entity.setReceivedAt(Instant.now());

        repository.save(entity);
        log.debug("OrderProjection saved — orderId={}", p.orderId());
    }

    // -------------------------------------------------------------------------
    // Message DTO — matches event-schemas.md OrderCreated envelope
    // -------------------------------------------------------------------------

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OrderCreatedMessage(
            String eventType,
            String eventId,
            String occurredAt,
            Payload payload
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Payload(
                UUID orderId,
                UUID buyerId,
                UUID eventEntityId,
                UUID dateId,
                BigDecimal total,
                BigDecimal serviceFee,
                String currency,
                String status
        ) {}
    }
}
