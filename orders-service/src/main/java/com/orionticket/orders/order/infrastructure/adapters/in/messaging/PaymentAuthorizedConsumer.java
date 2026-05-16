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
public class PaymentAuthorizedConsumer {

    private final OrderUseCase orderUseCase;

    // Cuando Payments confirma el pago, este servicio confirma la orden vinculada
    @RabbitListener(queues = RabbitMqConfig.QUEUE_PAYMENT_AUTHORIZED)
    public void handleEvent(Map<String, Object> message) {
        try {
            Map<String, Object> payload = extractPayload(message);
            UUID orderId   = UUID.fromString((String) payload.get("orderId"));
            UUID paymentId = UUID.fromString((String) payload.get("paymentId"));
            log.info("Processing PaymentAuthorized for order {}", orderId);
            orderUseCase.confirmOrder(orderId, paymentId);
        } catch (Exception e) {
            log.error("Error processing PaymentAuthorized event: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractPayload(Map<String, Object> message) {
        return (Map<String, Object>) message.get("payload");
    }
}
