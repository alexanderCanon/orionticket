package com.orionticket.orders.promotion.infrastructure.adapters.in.rest;

import com.orionticket.orders.promotion.application.port.in.PromotionUseCase;
import com.orionticket.orders.promotion.domain.model.Promotion;
import com.orionticket.orders.promotion.infrastructure.adapters.in.rest.dto.CreatePromotionRequest;
import com.orionticket.orders.promotion.infrastructure.adapters.in.rest.dto.PromotionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionUseCase promotionUseCase;

    // POST /v1/promotions — endpoint interno para que operadores creen códigos de descuento
    @PostMapping
    @PreAuthorize("hasAuthority('promotions:manage') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<PromotionResponse> createPromotion(
            @Valid @RequestBody CreatePromotionRequest request) {

        Promotion promotion = promotionUseCase.createPromotion(
                request.getEventId(),
                request.getCode(),
                request.getDiscountType(),
                request.getDiscountValue(),
                request.getMaxUses()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(PromotionResponse.from(promotion));
    }

    @GetMapping("/{promotionId}")
    @PreAuthorize("hasAuthority('promotions:manage') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<PromotionResponse> getPromotion(@PathVariable UUID promotionId) {
        return ResponseEntity.ok(PromotionResponse.from(promotionUseCase.getPromotionById(promotionId)));
    }
}
