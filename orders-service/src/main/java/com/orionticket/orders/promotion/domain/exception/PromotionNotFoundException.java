package com.orionticket.orders.promotion.domain.exception;

public class PromotionNotFoundException extends RuntimeException {
    public PromotionNotFoundException(String code) {
        super("Promotion code not found or not active for this event: " + code);
    }
}
