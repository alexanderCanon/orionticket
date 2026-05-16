package com.orionticket.orders.promotion.domain.exception;

public class InvalidPromotionCodeException extends RuntimeException {
    public InvalidPromotionCodeException(String code) {
        super("Invalid or inactive promotion code: " + code);
    }
}
