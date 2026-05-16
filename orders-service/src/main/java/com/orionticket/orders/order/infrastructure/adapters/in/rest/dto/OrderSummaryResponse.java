package com.orionticket.orders.order.infrastructure.adapters.in.rest.dto;

import com.orionticket.orders.order.domain.model.Order;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class OrderSummaryResponse {

    private UUID orderId;
    private UUID eventId;
    private BigDecimal total;
    private String currency;
    private String status;
    private Instant createdAt;

    public static OrderSummaryResponse from(Order order) {
        return OrderSummaryResponse.builder()
                .orderId(order.getOrderId())
                .eventId(order.getEventId())
                .total(order.getTotal())
                .currency(order.getCurrency())
                .status(order.getStatus().name())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
