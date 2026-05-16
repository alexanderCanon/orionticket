package com.orionticket.orders.promotion.infrastructure.adapters.in.rest.dto;

import com.orionticket.orders.promotion.domain.model.DiscountType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreatePromotionRequest {

    @NotNull
    private UUID eventId;

    @NotBlank
    private String code;

    @NotNull
    private DiscountType discountType;

    @NotNull
    @Min(0)
    private BigDecimal discountValue;

    @Min(1)
    private int maxUses;
}
