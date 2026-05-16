package com.orionticket.orders.order.infrastructure.adapters.in.rest.dto;

import com.orionticket.orders.order.domain.model.Order;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class OrderResponse {

    private UUID orderId;
    private UUID buyerId;
    private UUID eventId;
    private UUID dateId;
    private UUID reservationId;
    private List<LineItemResponse> lineItems;
    private BigDecimal subtotal;
    private UUID promotionId;
    private BigDecimal promotionDiscount;
    private BigDecimal serviceFee;
    private BigDecimal total;
    private String currency;
    private String status;
    private Instant createdAt;

    public static OrderResponse from(Order order) {
        List<LineItemResponse> items = order.getLineItems().stream()
                .map(li -> LineItemResponse.builder()
                        .lineItemId(li.getLineItemId())
                        .seatId(li.getSeatId())
                        .batchPrice(li.getBatchPrice())
                        .quantity(li.getQuantity())
                        .build())
                .toList();

        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .buyerId(order.getBuyerId())
                .eventId(order.getEventId())
                .dateId(order.getDateId())
                .reservationId(order.getReservationId())
                .lineItems(items)
                .subtotal(order.getSubtotal())
                .promotionId(order.getPromotionId())
                .promotionDiscount(order.getPromotionDiscount())
                .serviceFee(order.getServiceFee())
                .total(order.getTotal())
                .currency(order.getCurrency())
                .status(order.getStatus().name())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
