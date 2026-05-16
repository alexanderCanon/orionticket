package com.orionticket.orders.order.domain.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Agregado raíz de la orden. Sin dependencias de Spring/JPA — lógica pura de negocio.
@Data
@Builder
public class Order {

    private UUID orderId;
    private UUID buyerId;
    private UUID eventId;
    private UUID dateId;
    private UUID reservationId;

    @Builder.Default
    private List<LineItem> lineItems = new ArrayList<>();

    private BigDecimal subtotal;
    private UUID promotionId;          // null si no se aplicó promoción
    private BigDecimal promotionDiscount;
    private BigDecimal serviceFee;
    private BigDecimal total;
    private String currency;
    private OrderStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    // Factory: crea una nueva orden con status CREATED y timestamps actuales
    public static Order create(UUID buyerId, UUID eventId, UUID dateId, UUID reservationId,
                               List<LineItem> lineItems, BigDecimal subtotal,
                               UUID promotionId, BigDecimal promotionDiscount,
                               BigDecimal serviceFee, BigDecimal total, String currency) {
        Instant now = Instant.now();
        return Order.builder()
                .orderId(UUID.randomUUID())
                .buyerId(buyerId)
                .eventId(eventId)
                .dateId(dateId)
                .reservationId(reservationId)
                .lineItems(lineItems)
                .subtotal(subtotal)
                .promotionId(promotionId)
                .promotionDiscount(promotionDiscount)
                .serviceFee(serviceFee)
                .total(total)
                .currency(currency)
                .status(OrderStatus.CREATED)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void expire() {
        this.status = OrderStatus.EXPIRED;
        this.updatedAt = Instant.now();
    }

    public void confirm() {
        this.status = OrderStatus.CONFIRMED;
        this.updatedAt = Instant.now();
    }

    public boolean isExpired() {
        return this.status == OrderStatus.EXPIRED;
    }

    public boolean isConfirmed() {
        return this.status == OrderStatus.CONFIRMED;
    }
}
