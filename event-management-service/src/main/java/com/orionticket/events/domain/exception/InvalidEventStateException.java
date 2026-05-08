package com.orionticket.events.domain.exception;

public class InvalidEventStateException extends RuntimeException {
    public InvalidEventStateException(String message) {
        super(message);
    }
}
