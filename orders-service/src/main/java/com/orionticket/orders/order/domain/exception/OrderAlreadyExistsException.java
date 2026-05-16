package com.orionticket.orders.order.domain.exception;

import java.util.UUID;

// Se lanza cuando ya existe una orden para la misma reserva — garantiza idempotencia (ADR-008)
public class OrderAlreadyExistsException extends RuntimeException {
    public OrderAlreadyExistsException(UUID reservationId) {
        super("Order already exists for reservation: " + reservationId);
    }
}
