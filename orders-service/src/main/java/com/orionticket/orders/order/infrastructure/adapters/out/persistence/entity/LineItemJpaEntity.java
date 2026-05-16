package com.orionticket.orders.order.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "line_items")
@Data
@NoArgsConstructor
public class LineItemJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID lineItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderJpaEntity order;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID seatId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal batchPrice;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private Instant createdAt;
}
