package com.orionticket.orders.order.infrastructure.adapters.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orionticket.orders.order.application.port.in.OrderUseCase;
import com.orionticket.orders.order.domain.model.ReservationSnapshot;
import com.orionticket.orders.shared.infrastructure.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationCreatedConsumer {

    private final OrderUseCase orderUseCase;
    private final ObjectMapper objectMapper;

    // Guarda snapshot local cuando seating-inventory crea una reserva.
    // Esto desacopla el checkout de llamadas síncronas a otro servicio.
    @RabbitListener(queues = RabbitMqConfig.QUEUE_RESERVATION_CREATED)
    public void handleEvent(Map<String, Object> message) {
        try {
            Map<String, Object> payload = extractPayload(message);

            ReservationSnapshot snapshot = ReservationSnapshot.builder()
                    .reservationId(UUID.fromString((String) payload.get("reservationId")))
                    .seatId(UUID.fromString((String) payload.get("seatId")))
                    .batchId(UUID.fromString((String) payload.get("batchId")))
                    .batchPrice(new BigDecimal(payload.get("batchPrice").toString()))
                    .buyerId(UUID.fromString((String) payload.get("buyerId")))
                    .eventId(UUID.fromString((String) payload.get("eventEntityId")))
                    .dateId(UUID.fromString((String) payload.get("dateId")))
                    .expiresAt(Instant.parse((String) payload.get("expiresAt")))
                    .receivedAt(Instant.now())
                    .build();

            orderUseCase.storeReservationSnapshot(snapshot);
            log.info("Snapshot stored for reservation {}", snapshot.getReservationId());

        } catch (Exception e) {
            log.error("Error processing ReservationCreated event: {}", e.getMessage(), e);
            throw new RuntimeException(e); // permite reintentos por la retry policy
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractPayload(Map<String, Object> message) {
        return (Map<String, Object>) message.get("payload");
    }
}
