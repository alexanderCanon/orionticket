package com.orionticket.orders.promotion.infrastructure.adapters.in.rest.dto;

import com.orionticket.orders.promotion.domain.model.Promotion;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class PromotionResponse {
    private UUID promotionId;
    private UUID eventId;
    private String code;
    private String discountType;
    private BigDecimal discountValue;
    private int maxUses;
    private int usedCount;
    private String status;

    public static PromotionResponse from(Promotion promotion) {
        return PromotionResponse.builder()
                .promotionId(promotion.getPromotionId())
                .eventId(promotion.getEventId())
                .code(promotion.getCode())
                .discountType(promotion.getDiscountType().name())
                .discountValue(promotion.getDiscountValue())
                .maxUses(promotion.getMaxUses())
                .usedCount(promotion.getUsedCount())
                .status(promotion.getStatus().name())
                .build();
    }
}
