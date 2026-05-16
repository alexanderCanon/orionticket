package com.orionticket.orders.order.infrastructure.adapters.out.messaging;

import com.orionticket.orders.order.domain.model.Order;
import com.orionticket.orders.order.domain.port.out.DomainEventPublisherPort;
import com.orionticket.orders.promotion.domain.model.Promotion;
import com.orionticket.orders.shared.infrastructure.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMqEventPublisherAdapter implements DomainEventPublisherPort {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishOrderCreated(Order order) {
        Map<String, Object> lineItems = new java.util.LinkedHashMap<>();
        List<Map<String, Object>> items = order.getLineItems().stream()
                .map(li -> {
                    Map<String, Object> item = new java.util.LinkedHashMap<>();
                    item.put("lineItemId", li.getLineItemId().toString());
                    item.put("seatId", li.getSeatId().toString());
                    item.put("batchPrice", li.getBatchPrice());
                    item.put("quantity", li.getQuantity());
                    return item;
                }).collect(Collectors.toList());

        Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("orderId", order.getOrderId().toString());
        payload.put("buyerId", order.getBuyerId().toString());
        payload.put("eventEntityId", order.getEventId().toString());
        payload.put("dateId", order.getDateId().toString());
        payload.put("reservationId", order.getReservationId().toString());
        payload.put("lineItems", items);
        payload.put("subtotal", order.getSubtotal());
        payload.put("promotionId", order.getPromotionId() != null ? order.getPromotionId().toString() : null);
        payload.put("promotionDiscount", order.getPromotionDiscount());
        payload.put("serviceFee", order.getServiceFee());
        payload.put("total", order.getTotal());
        payload.put("currency", order.getCurrency());
        payload.put("status", order.getStatus().name());

        publish(RabbitMqConfig.RK_ORDER_CREATED, "OrderCreated", payload);
        log.info("Published OrderCreated for order {}", order.getOrderId());
    }

    @Override
    public void publishOrderExpired(Order order) {
        Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("orderId", order.getOrderId().toString());
        payload.put("buyerId", order.getBuyerId().toString());
        payload.put("reservationId", order.getReservationId().toString());
        payload.put("status", "EXPIRED");

        publish(RabbitMqConfig.RK_ORDER_EXPIRED, "OrderExpired", payload);
        log.info("Published OrderExpired for order {}", order.getOrderId());
    }

    @Override
    public void publishOrderConfirmed(Order order, UUID paymentId) {
        Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("orderId", order.getOrderId().toString());
        payload.put("buyerId", order.getBuyerId().toString());
        payload.put("reservationId", order.getReservationId().toString());
        payload.put("paymentId", paymentId.toString());
        payload.put("total", order.getTotal());
        payload.put("currency", order.getCurrency());
        payload.put("status", "CONFIRMED");

        publish(RabbitMqConfig.RK_ORDER_CONFIRMED, "OrderConfirmed", payload);
        log.info("Published OrderConfirmed for order {} with payment {}", order.getOrderId(), paymentId);
    }

    @Override
    public void publishPromotionExhausted(Promotion promotion) {
        Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("promotionId", promotion.getPromotionId().toString());
        payload.put("eventId", promotion.getEventId().toString());
        payload.put("code", promotion.getCode());
        payload.put("usedCount", promotion.getUsedCount());
        payload.put("status", "EXHAUSTED");

        publish(RabbitMqConfig.RK_PROMOTION_EXHAUSTED, "PromotionExhausted", payload);
        log.info("Published PromotionExhausted for code {}", promotion.getCode());
    }

    // Sobre de evento estándar — todos los eventos tienen el mismo wrapper
    private void publish(String routingKey, String eventType, Map<String, Object> payload) {
        Map<String, Object> envelope = new java.util.LinkedHashMap<>();
        envelope.put("eventType", eventType);
        envelope.put("eventId", UUID.randomUUID().toString());
        envelope.put("occurredAt", Instant.now().toString());
        envelope.put("payload", payload);
        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE, routingKey, envelope);
    }
}
