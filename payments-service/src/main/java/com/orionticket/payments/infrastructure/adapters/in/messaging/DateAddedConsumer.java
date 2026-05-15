package com.orionticket.payments.infrastructure.adapters.in.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.orionticket.payments.infrastructure.adapters.out.persistence.entity.DateProjectionEntity;
import com.orionticket.payments.infrastructure.adapters.out.persistence.repository.SpringDataDateProjectionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Consumes DateAdded events from the Event Management service.
 * Records Date schedule data so the payout scheduler can detect
 * when a Date has passed and trigger Payout generation (ADR-009).
 *
 * Event schema: docs/phases/phase-3/event-schemas.md — DateAdded
 */
@Component
public class DateAddedConsumer {

    private static final Logger log = LoggerFactory.getLogger(DateAddedConsumer.class);

    private final SpringDataDateProjectionRepository repository;

    public DateAddedConsumer(SpringDataDateProjectionRepository repository) {
        this.repository = repository;
    }

    @RabbitListener(queues = "${rabbitmq.queues.date-added:payments.queue.date-added}")
    @Transactional
    public void consume(DateAddedMessage message) {
        if (message == null || message.payload() == null) {
            log.warn("Received null or empty DateAdded message — skipping");
            return;
        }

        DateAddedMessage.Payload p = message.payload();
        log.info("Received DateAdded — dateId={} eventId={} scheduledAt={}",
                p.dateId(), p.eventEntityId(), p.scheduledAt());

        // Idempotent: if already exists, do not overwrite payoutGenerated flag
        if (repository.existsById(p.dateId())) {
            log.debug("DateProjection already exists — dateId={} skipping", p.dateId());
            return;
        }

        DateProjectionEntity entity = new DateProjectionEntity();
        entity.setDateId(p.dateId());
        entity.setEventId(p.eventEntityId());
        entity.setScheduledAt(Instant.parse(p.scheduledAt()));
        entity.setPayoutGenerated(false);
        entity.setReceivedAt(Instant.now());

        repository.save(entity);
        log.debug("DateProjection saved — dateId={}", p.dateId());
    }

    // -------------------------------------------------------------------------
    // Message DTO — matches event-schemas.md DateAdded envelope
    // -------------------------------------------------------------------------

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DateAddedMessage(
            String eventType,
            String eventId,
            String occurredAt,
            Payload payload
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Payload(
                UUID dateId,
                UUID eventEntityId,
                String scheduledAt,   // ISO-8601 string; parsed to Instant on save
                UUID venueId,
                Integer capacity
        ) {}
    }
}
