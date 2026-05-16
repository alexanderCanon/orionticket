package com.orionticket.orders.promotion.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "promotions")
@Data
@NoArgsConstructor
public class PromotionJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID promotionId;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID eventId;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 20)
    private String discountType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(nullable = false)
    private int maxUses;

    @Column(nullable = false)
    private int usedCount;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;
}
