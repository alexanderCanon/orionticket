package com.orionticket.orders.order.domain.port.out;

import com.orionticket.orders.order.domain.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

// Puerto de salida de persistencia — el dominio solo conoce esta interfaz, no JPA
public interface OrderRepositoryPort {
    Order save(Order order);
    Optional<Order> findById(UUID orderId);
    Optional<Order> findByReservationId(UUID reservationId);
    Page<Order> findByBuyerId(UUID buyerId, Pageable pageable);
}
