package com.orionticket.orders.promotion.application.port.in;

import com.orionticket.orders.promotion.domain.model.DiscountType;
import com.orionticket.orders.promotion.domain.model.Promotion;

import java.math.BigDecimal;
import java.util.UUID;

public interface PromotionUseCase {
    Promotion createPromotion(UUID eventId, String code, DiscountType discountType,
                              BigDecimal discountValue, int maxUses);
    Promotion getPromotionById(UUID promotionId);
}
