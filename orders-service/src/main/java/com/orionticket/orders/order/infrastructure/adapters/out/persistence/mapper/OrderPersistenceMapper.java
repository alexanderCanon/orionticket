package com.orionticket.orders.order.infrastructure.adapters.out.persistence.mapper;

import com.orionticket.orders.order.domain.model.LineItem;
import com.orionticket.orders.order.domain.model.Order;
import com.orionticket.orders.order.domain.model.OrderStatus;
import com.orionticket.orders.order.domain.model.ReservationSnapshot;
import com.orionticket.orders.order.infrastructure.adapters.out.persistence.entity.LineItemJpaEntity;
import com.orionticket.orders.order.infrastructure.adapters.out.persistence.entity.OrderJpaEntity;
import com.orionticket.orders.order.infrastructure.adapters.out.persistence.entity.ReservationSnapshotJpaEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class OrderPersistenceMapper {

    public OrderJpaEntity toEntity(Order order) {
        OrderJpaEntity entity = new OrderJpaEntity();
        entity.setOrderId(order.getOrderId());
        entity.setBuyerId(order.getBuyerId());
        entity.setEventId(order.getEventId());
        entity.setDateId(order.getDateId());
        entity.setReservationId(order.getReservationId());
        entity.setStatus(order.getStatus().name());
        entity.setSubtotal(order.getSubtotal());
        entity.setPromotionId(order.getPromotionId());
        entity.setPromotionDiscount(order.getPromotionDiscount());
        entity.setServiceFee(order.getServiceFee());
        entity.setTotal(order.getTotal());
        entity.setCurrency(order.getCurrency());
        entity.setCreatedAt(order.getCreatedAt());
        entity.setUpdatedAt(order.getUpdatedAt());

        List<LineItemJpaEntity> lineItemEntities = order.getLineItems().stream()
                .map(li -> toLineItemEntity(li, entity))
                .toList();
        entity.getLineItems().addAll(lineItemEntities);

        return entity;
    }

    public Order toDomain(OrderJpaEntity entity) {
        List<LineItem> lineItems = entity.getLineItems().stream()
                .map(this::toLineItemDomain)
                .toList();

        return Order.builder()
                .orderId(entity.getOrderId())
                .buyerId(entity.getBuyerId())
                .eventId(entity.getEventId())
                .dateId(entity.getDateId())
                .reservationId(entity.getReservationId())
                .lineItems(new java.util.ArrayList<>(lineItems))
                .status(OrderStatus.valueOf(entity.getStatus()))
                .subtotal(entity.getSubtotal())
                .promotionId(entity.getPromotionId())
                .promotionDiscount(entity.getPromotionDiscount())
                .serviceFee(entity.getServiceFee())
                .total(entity.getTotal())
                .currency(entity.getCurrency())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private LineItemJpaEntity toLineItemEntity(LineItem lineItem, OrderJpaEntity orderEntity) {
        LineItemJpaEntity entity = new LineItemJpaEntity();
        entity.setLineItemId(lineItem.getLineItemId());
        entity.setOrder(orderEntity);
        entity.setSeatId(lineItem.getSeatId());
        entity.setBatchPrice(lineItem.getBatchPrice());
        entity.setQuantity(lineItem.getQuantity());
        entity.setCreatedAt(Instant.now());
        return entity;
    }

    private LineItem toLineItemDomain(LineItemJpaEntity entity) {
        return LineItem.builder()
                .lineItemId(entity.getLineItemId())
                .orderId(entity.getOrder().getOrderId())
                .seatId(entity.getSeatId())
                .batchPrice(entity.getBatchPrice())
                .quantity(entity.getQuantity())
                .build();
    }

    public ReservationSnapshotJpaEntity toSnapshotEntity(ReservationSnapshot snapshot) {
        ReservationSnapshotJpaEntity entity = new ReservationSnapshotJpaEntity();
        entity.setReservationId(snapshot.getReservationId());
        entity.setSeatId(snapshot.getSeatId());
        entity.setBatchId(snapshot.getBatchId());
        entity.setBatchPrice(snapshot.getBatchPrice());
        entity.setBuyerId(snapshot.getBuyerId());
        entity.setEventId(snapshot.getEventId());
        entity.setDateId(snapshot.getDateId());
        entity.setExpiresAt(snapshot.getExpiresAt());
        entity.setReceivedAt(snapshot.getReceivedAt() != null ? snapshot.getReceivedAt() : Instant.now());
        return entity;
    }

    public ReservationSnapshot toSnapshotDomain(ReservationSnapshotJpaEntity entity) {
        return ReservationSnapshot.builder()
                .reservationId(entity.getReservationId())
                .seatId(entity.getSeatId())
                .batchId(entity.getBatchId())
                .batchPrice(entity.getBatchPrice())
                .buyerId(entity.getBuyerId())
                .eventId(entity.getEventId())
                .dateId(entity.getDateId())
                .expiresAt(entity.getExpiresAt())
                .receivedAt(entity.getReceivedAt())
                .build();
    }
}
