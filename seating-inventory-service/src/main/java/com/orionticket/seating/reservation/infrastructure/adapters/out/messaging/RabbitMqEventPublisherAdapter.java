package com.orionticket.seating.reservation.infrastructure.adapters.out.messaging;

import com.orionticket.seating.batch.domain.model.Batch;
import com.orionticket.seating.reservation.domain.model.Reservation;
import com.orionticket.seating.reservation.domain.port.out.DomainEventPublisherPort;
import com.orionticket.seating.shared.infrastructure.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Adapter de salida: implementa DomainEventPublisherPort usando RabbitMQ.
// El dominio solo conoce la interfaz del puerto; este adapter sabe del exchange y las routing keys.
// Todos los eventos siguen el mismo envelope: { eventType, eventId, occurredAt, payload }.
@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMqEventPublisherAdapter implements DomainEventPublisherPort {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishReservationCreated(Reservation reservation, java.math.BigDecimal batchPrice) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("reservationId", reservation.getReservationId());
        payload.put("seatId", reservation.getSeatId());
        payload.put("buyerId", reservation.getBuyerId());
        payload.put("eventId", reservation.getEventId());
        payload.put("dateId", reservation.getDateId());
        payload.put("batchId", reservation.getBatchId());
        payload.put("batchPrice", batchPrice);
        payload.put("expiresAt", reservation.getExpiresAt().toString());

        publish(RabbitMqConfig.RESERVATION_CREATED_KEY, "ReservationCreated", payload);
    }

    @Override
    public void publishReservationExpired(Reservation reservation) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("reservationId", reservation.getReservationId());
        payload.put("seatId", reservation.getSeatId());
        payload.put("buyerId", reservation.getBuyerId());
        payload.put("eventId", reservation.getEventId());
        payload.put("dateId", reservation.getDateId());
        payload.put("batchId", reservation.getBatchId());

        publish(RabbitMqConfig.RESERVATION_EXPIRED_KEY, "ReservationExpired", payload);
    }

    @Override
    public void publishReservationReleased(Reservation reservation) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("reservationId", reservation.getReservationId());
        payload.put("seatId", reservation.getSeatId());
        payload.put("buyerId", reservation.getBuyerId());
        payload.put("eventId", reservation.getEventId());

        publish(RabbitMqConfig.RESERVATION_RELEASED_KEY, "ReservationReleased", payload);
    }

    @Override
    public void publishBatchCreated(Batch batch) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("batchId", batch.getBatchId());
        payload.put("eventId", batch.getEventId());
        payload.put("dateId", batch.getDateId());
        payload.put("name", batch.getName());
        payload.put("price", batch.getPrice());
        payload.put("currency", batch.getCurrency());
        payload.put("capacity", batch.getCapacity());
        payload.put("status", batch.getStatus());

        publish(RabbitMqConfig.BATCH_CREATED_KEY, "BatchCreated", payload);
    }

    @Override
    public void publishBatchActivated(Batch batch) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("batchId", batch.getBatchId());
        payload.put("eventId", batch.getEventId());
        payload.put("dateId", batch.getDateId());

        publish(RabbitMqConfig.BATCH_ACTIVATED_KEY, "BatchActivated", payload);
    }

    @Override
    public void publishBatchExhausted(Batch batch) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("batchId", batch.getBatchId());
        payload.put("eventId", batch.getEventId());
        payload.put("dateId", batch.getDateId());
        payload.put("sold", batch.getSold());
        payload.put("capacity", batch.getCapacity());

        publish(RabbitMqConfig.BATCH_EXHAUSTED_KEY, "BatchExhausted", payload);
    }

    // Envelope estándar OrionTicket: todos los eventos tienen la misma estructura exterior.
    // eventId es el ID del MENSAJE (no del evento de negocio) para deduplicación.
    // occurredAt: cuándo se generó este mensaje (no cuándo ocurrió el hecho de negocio).
    private void publish(String routingKey, String eventType, Map<String, Object> payload) {
        Map<String, Object> envelope = new HashMap<>();
        envelope.put("eventType", eventType);
        envelope.put("eventId", UUID.randomUUID().toString());
        envelope.put("occurredAt", ZonedDateTime.now().toString());
        envelope.put("payload", payload);

        log.info("Publishing event {} with routing key {}", eventType, routingKey);
        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE_NAME, routingKey, envelope);
    }
}
