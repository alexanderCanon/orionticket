package com.orionticket.orders.promotion.application.service;

import com.orionticket.orders.promotion.domain.exception.PromotionNotFoundException;
import com.orionticket.orders.promotion.domain.model.DiscountType;
import com.orionticket.orders.promotion.domain.model.Promotion;
import com.orionticket.orders.promotion.domain.model.PromotionStatus;
import com.orionticket.orders.promotion.domain.port.out.PromotionRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromotionServiceTest {

    @Mock private PromotionRepositoryPort promotionRepository;
    @InjectMocks private PromotionService promotionService;

    @Test
    void createPromotion_shouldPersistWithStatusActive() {
        UUID eventId = UUID.randomUUID();
        when(promotionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Promotion result = promotionService.createPromotion(
                eventId, "VERANO10", DiscountType.PERCENTAGE, new BigDecimal("10"), 100);

        assertThat(result.getStatus()).isEqualTo(PromotionStatus.ACTIVE);
        assertThat(result.getCode()).isEqualTo("VERANO10");
        assertThat(result.getUsedCount()).isEqualTo(0);
        verify(promotionRepository).save(any(Promotion.class));
    }

    @Test
    void applyPromotion_percentage_shouldCalculateCorrectly() {
        Promotion promo = buildPromotion(DiscountType.PERCENTAGE, new BigDecimal("15"));
        BigDecimal subtotal = new BigDecimal("200.00");

        BigDecimal discount = promo.calculateDiscount(subtotal);

        assertThat(discount).isEqualByComparingTo("30.00"); // 15% de 200
    }

    @Test
    void applyPromotion_fixed_shouldNotExceedSubtotal() {
        Promotion promo = buildPromotion(DiscountType.FIXED, new BigDecimal("500"));
        BigDecimal subtotal = new BigDecimal("100.00");

        BigDecimal discount = promo.calculateDiscount(subtotal);

        assertThat(discount).isEqualByComparingTo("100.00"); // capped al subtotal
    }

    @Test
    void applyPromotion_whenMaxUsesReached_shouldMarkExhausted() {
        Promotion promo = buildPromotion(DiscountType.PERCENTAGE, new BigDecimal("10"));
        promo.setMaxUses(1);
        promo.setUsedCount(0);
        promo.setStatus(PromotionStatus.ACTIVE);

        promo.incrementUsed();

        assertThat(promo.getUsedCount()).isEqualTo(1);
        assertThat(promo.getStatus()).isEqualTo(PromotionStatus.EXHAUSTED);
        assertThat(promo.isAvailable()).isFalse();
    }

    @Test
    void getPromotionById_whenNotFound_shouldThrow() {
        UUID id = UUID.randomUUID();
        when(promotionRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> promotionService.getPromotionById(id))
                .isInstanceOf(PromotionNotFoundException.class);
    }

    private Promotion buildPromotion(DiscountType type, BigDecimal value) {
        return Promotion.builder()
                .promotionId(UUID.randomUUID())
                .eventId(UUID.randomUUID())
                .code("TEST")
                .discountType(type)
                .discountValue(value)
                .maxUses(10)
                .usedCount(0)
                .status(PromotionStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
