package com.orionticket.orders.order.domain.port.out;

import com.orionticket.orders.order.domain.model.Order;
import com.orionticket.orders.promotion.domain.model.Promotion;

import java.util.UUID;

// Puerto de salida de mensajería — el dominio no conoce RabbitMQ ni ningun broker concreto
public interface DomainEventPublisherPort {
    void publishOrderCreated(Order order);
    void publishOrderExpired(Order order);
    void publishOrderConfirmed(Order order, UUID paymentId);
    void publishPromotionExhausted(Promotion promotion);
}
