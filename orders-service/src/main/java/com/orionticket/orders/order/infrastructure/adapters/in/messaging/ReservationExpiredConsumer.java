package com.orionticket.orders.order.infrastructure.adapters.in.messaging;

import com.orionticket.orders.order.application.port.in.OrderUseCase;
import com.orionticket.orders.shared.infrastructure.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationExpiredConsumer {

    private final OrderUseCase orderUseCase;

    // Cuando seating-inventory expira una reserva, este servicio expira la orden vinculada
    @RabbitListener(queues = RabbitMqConfig.QUEUE_RESERVATION_EXPIRED)
    public void handleEvent(Map<String, Object> message) {
        try {
            Map<String, Object> payload = extractPayload(message);
            UUID reservationId = UUID.fromString((String) payload.get("reservationId"));
            log.info("Processing ReservationExpired for reservation {}", reservationId);
            orderUseCase.expireOrderByReservation(reservationId);
        } catch (Exception e) {
            log.error("Error processing ReservationExpired event: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractPayload(Map<String, Object> message) {
        return (Map<String, Object>) message.get("payload");
    }
}
