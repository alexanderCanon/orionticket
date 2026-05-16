package com.orionticket.orders.promotion.domain.exception;

public class PromotionExhaustedException extends RuntimeException {
    public PromotionExhaustedException(String code) {
        super("Promotion code is exhausted: " + code);
    }
}
