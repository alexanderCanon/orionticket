package com.orionticket.orders.promotion.domain.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

// Agregado de Promoción — contiene la lógica de descuento y control de usos
@Data
@Builder
public class Promotion {

    private UUID promotionId;
    private UUID eventId;
    private String code;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private int maxUses;
    private int usedCount;
    private PromotionStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public static Promotion create(UUID eventId, String code, DiscountType discountType,
                                   BigDecimal discountValue, int maxUses) {
        Instant now = Instant.now();
        return Promotion.builder()
                .promotionId(UUID.randomUUID())
                .eventId(eventId)
                .code(code.toUpperCase())
                .discountType(discountType)
                .discountValue(discountValue)
                .maxUses(maxUses)
                .usedCount(0)
                .status(PromotionStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public boolean isAvailable() {
        return this.status == PromotionStatus.ACTIVE && this.usedCount < this.maxUses;
    }

    // Calcula el descuento sobre el subtotal dado. Nunca excede el subtotal.
    public BigDecimal calculateDiscount(BigDecimal subtotal) {
        if (discountType == DiscountType.PERCENTAGE) {
            return subtotal.multiply(discountValue).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        // FIXED: descuento fijo, pero no puede superar el subtotal
        return discountValue.min(subtotal);
    }

    // Incrementa el contador y marca EXHAUSTED si se alcanzó el máximo
    public void incrementUsed() {
        this.usedCount++;
        this.updatedAt = Instant.now();
        if (this.usedCount >= this.maxUses) {
            this.status = PromotionStatus.EXHAUSTED;
        }
    }
}
