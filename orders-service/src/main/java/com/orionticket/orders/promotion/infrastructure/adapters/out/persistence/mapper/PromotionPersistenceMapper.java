package com.orionticket.orders.promotion.infrastructure.adapters.out.persistence.mapper;

import com.orionticket.orders.promotion.domain.model.DiscountType;
import com.orionticket.orders.promotion.domain.model.Promotion;
import com.orionticket.orders.promotion.domain.model.PromotionStatus;
import com.orionticket.orders.promotion.infrastructure.adapters.out.persistence.entity.PromotionJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class PromotionPersistenceMapper {

    public PromotionJpaEntity toEntity(Promotion promotion) {
        PromotionJpaEntity entity = new PromotionJpaEntity();
        entity.setPromotionId(promotion.getPromotionId());
        entity.setEventId(promotion.getEventId());
        entity.setCode(promotion.getCode());
        entity.setDiscountType(promotion.getDiscountType().name());
        entity.setDiscountValue(promotion.getDiscountValue());
        entity.setMaxUses(promotion.getMaxUses());
        entity.setUsedCount(promotion.getUsedCount());
        entity.setStatus(promotion.getStatus().name());
        entity.setCreatedAt(promotion.getCreatedAt());
        entity.setUpdatedAt(promotion.getUpdatedAt());
        return entity;
    }

    public Promotion toDomain(PromotionJpaEntity entity) {
        return Promotion.builder()
                .promotionId(entity.getPromotionId())
                .eventId(entity.getEventId())
                .code(entity.getCode())
                .discountType(DiscountType.valueOf(entity.getDiscountType()))
                .discountValue(entity.getDiscountValue())
                .maxUses(entity.getMaxUses())
                .usedCount(entity.getUsedCount())
                .status(PromotionStatus.valueOf(entity.getStatus()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
