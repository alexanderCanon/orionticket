package com.orionticket.orders.order.domain.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

// Ítem de línea de una orden: un asiento reservado con su precio de tanda
@Data
@Builder
public class LineItem {

    private UUID lineItemId;
    private UUID orderId;
    private UUID seatId;
    private BigDecimal batchPrice;
    private int quantity;

    public static LineItem create(UUID orderId, UUID seatId, BigDecimal batchPrice) {
        return LineItem.builder()
                .lineItemId(UUID.randomUUID())
                .orderId(orderId)
                .seatId(seatId)
                .batchPrice(batchPrice)
                .quantity(1)    // un asiento = un ítem
                .build();
    }

    public BigDecimal subtotal() {
        return batchPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
