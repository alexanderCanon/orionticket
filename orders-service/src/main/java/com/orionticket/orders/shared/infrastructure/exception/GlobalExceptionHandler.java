package com.orionticket.orders.shared.infrastructure.exception;

import com.orionticket.orders.order.domain.exception.OrderAlreadyExistsException;
import com.orionticket.orders.order.domain.exception.OrderNotFoundException;
import com.orionticket.orders.order.domain.exception.ReservationSnapshotNotFoundException;
import com.orionticket.orders.promotion.domain.exception.InvalidPromotionCodeException;
import com.orionticket.orders.promotion.domain.exception.PromotionExhaustedException;
import com.orionticket.orders.promotion.domain.exception.PromotionNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({OrderNotFoundException.class, ReservationSnapshotNotFoundException.class,
                       PromotionNotFoundException.class})
    public ResponseEntity<Map<String, Object>> handleNotFound(RuntimeException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // Una reserva solo puede tener una orden — idempotencia (ADR-008)
    @ExceptionHandler(OrderAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(OrderAlreadyExistsException ex) {
        return error(HttpStatus.CONFLICT, ex.getMessage());
    }

    // Código de promoción inválido, agotado o no aplicable al evento
    @ExceptionHandler({InvalidPromotionCodeException.class, PromotionExhaustedException.class})
    public ResponseEntity<Map<String, Object>> handleUnprocessable(RuntimeException ex) {
        return error(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst().orElse("Validation error");
        return error(HttpStatus.BAD_REQUEST, msg);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message,
                "timestamp", Instant.now().toString()
        ));
    }
}
