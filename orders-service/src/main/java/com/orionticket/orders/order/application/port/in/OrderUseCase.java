package com.orionticket.orders.order.application.port.in;

import com.orionticket.orders.order.domain.model.Order;
import com.orionticket.orders.order.domain.model.ReservationSnapshot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

// Puerto de entrada — lo que el mundo exterior puede pedirle a este servicio
public interface OrderUseCase {
    Order createOrder(CreateOrderCommand command);
    Order getOrderById(UUID orderId);
    Page<Order> getOrdersByBuyer(UUID buyerId, Pageable pageable);
    void storeReservationSnapshot(ReservationSnapshot snapshot);
    void expireOrderByReservation(UUID reservationId);
    void confirmOrder(UUID orderId, UUID paymentId);
}
