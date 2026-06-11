package com.orionticket.seating.reservation.infrastructure.adapters.in.messaging;

import com.orionticket.seating.reservation.application.port.in.ReservationUseCase;
import com.orionticket.seating.shared.infrastructure.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

// Consumer del evento PaymentFailed publicado por Payments Service.
// Cuando un pago falla, liberamos el asiento para que otro comprador pueda tomarlo.
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentFailedConsumer {

    private final ReservationUseCase reservationUseCase;

    @SuppressWarnings("unchecked")
    @RabbitListener(queues = RabbitMqConfig.PAYMENT_FAILED_QUEUE)
    public void handlePaymentFailed(Map<String, Object> message) {
        log.info("PaymentFailed event received: {}", message);
        try {
            Map<String, Object> payload = (Map<String, Object>) message.get("payload");
            String reservationIdStr = (String) payload.get("reservationId");

            if (reservationIdStr == null) {
                log.warn("PaymentFailed event without reservationId, skipping");
                return;
            }

            UUID reservationId = UUID.fromString(reservationIdStr);
            reservationUseCase.releaseReservation(reservationId);
            log.info("Reservation {} released after PaymentFailed", reservationId);

        } catch (Exception e) {
            // Si falla, RabbitMQ reintentará (según la config de retry en RabbitMqConfig).
            // Después de 3 intentos el mensaje va a la DLQ para inspección manual.
            log.error("Failed to handle PaymentFailed event: {}", e.getMessage());
            throw e; // relanzar para que el retry mechanism funcione
        }
    }
}
