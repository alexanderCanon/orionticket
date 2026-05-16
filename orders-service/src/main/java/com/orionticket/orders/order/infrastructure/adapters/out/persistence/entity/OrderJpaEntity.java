package com.orionticket.orders.order.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
public class OrderJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID orderId;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID buyerId;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID eventId;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID dateId;

    @Column(nullable = false, unique = true, columnDefinition = "uuid")
    private UUID reservationId;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(columnDefinition = "uuid")
    private UUID promotionId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal promotionDiscount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal serviceFee;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    // Carga por JOIN — siempre queremos los line items con la orden
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<LineItemJpaEntity> lineItems = new ArrayList<>();
}
