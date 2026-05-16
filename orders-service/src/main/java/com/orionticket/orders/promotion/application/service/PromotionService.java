package com.orionticket.orders.promotion.application.service;

import com.orionticket.orders.promotion.application.port.in.PromotionUseCase;
import com.orionticket.orders.promotion.domain.exception.PromotionNotFoundException;
import com.orionticket.orders.promotion.domain.model.DiscountType;
import com.orionticket.orders.promotion.domain.model.Promotion;
import com.orionticket.orders.promotion.domain.port.out.PromotionRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PromotionService implements PromotionUseCase {

    private final PromotionRepositoryPort promotionRepository;

    @Transactional
    @Override
    public Promotion createPromotion(UUID eventId, String code, DiscountType discountType,
                                     BigDecimal discountValue, int maxUses) {
        Promotion promotion = Promotion.create(eventId, code, discountType, discountValue, maxUses);
        return promotionRepository.save(promotion);
    }

    @Transactional(readOnly = true)
    @Override
    public Promotion getPromotionById(UUID promotionId) {
        return promotionRepository.findById(promotionId)
                .orElseThrow(() -> new PromotionNotFoundException(promotionId.toString()));
    }
}
