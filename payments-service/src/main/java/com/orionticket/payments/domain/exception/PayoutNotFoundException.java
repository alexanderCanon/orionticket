package com.orionticket.payments.domain.exception;

public class PayoutNotFoundException extends RuntimeException {

    public PayoutNotFoundException(String message) {
        super(message);
    }
}