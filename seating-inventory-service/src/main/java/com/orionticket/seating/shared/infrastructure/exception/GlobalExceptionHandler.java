package com.orionticket.seating.shared.infrastructure.exception;

import com.orionticket.seating.batch.domain.exception.BatchNotFoundException;
import com.orionticket.seating.reservation.domain.exception.BatchExhaustedException;
import com.orionticket.seating.reservation.domain.exception.ReservationConflictException;
import com.orionticket.seating.reservation.domain.exception.ReservationNotFoundException;
import com.orionticket.seating.seat.domain.exception.SeatNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

// @RestControllerAdvice: AOP que intercepta excepciones lanzadas por cualquier @RestController.
// Sin esto, Spring devuelve su error genérico (JSON interno sin estructura consistente).
// Centralizar aquí garantiza que TODOS los errores de la API tengan el mismo formato:
// { timestamp, status, error, message }.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 NOT FOUND: el recurso solicitado no existe en la base de datos.
    // POR QUÉ 404 y no 400: el request está bien formado, pero el ID no existe.
    // 400 significa "tu request está mal construido"; 404 es "no lo encuentro".
    @ExceptionHandler({SeatNotFoundException.class, BatchNotFoundException.class, ReservationNotFoundException.class})
    public ResponseEntity<Map<String, Object>> handleNotFound(RuntimeException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // 409 CONFLICT: el asiento ya está RESERVED o SOLD cuando intentas reservarlo.
    // POR QUÉ 409 y no 400: el request es válido (seatId existe, buyerId existe),
    // pero entra en conflicto con el estado actual del servidor.
    // Es un error RECUPERABLE: el comprador puede elegir otro asiento.
    @ExceptionHandler(ReservationConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(RuntimeException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    // 410 GONE: el Batch está EXHAUSTED (sold == capacity) o EXPIRED (fuera de ventana).
    // POR QUÉ 410 y no 409: GONE indica que el recurso existió pero ya no estará disponible.
    // Un asiento puede liberarse (409 es transitorio); un batch exhausto es definitivo.
    // El frontend debe mostrar "Esta tanda se agotó" y no ofrecer reintentar.
    @ExceptionHandler(BatchExhaustedException.class)
    public ResponseEntity<Map<String, Object>> handleGone(RuntimeException ex) {
        return buildResponse(HttpStatus.GONE, ex.getMessage());
    }

    // 422 UNPROCESSABLE ENTITY: el JSON llegó bien formado pero falló validación de campos
    // (@NotNull, @Min, @Size, @NotBlank, etc. de Bean Validation).
    // Devolvemos campo → mensaje para que el frontend muestre errores inline en el formulario.
    // POR QUÉ 422 y no 400: 400 es error de parseo del JSON; 422 es error semántico de negocio.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", ZonedDateTime.now());
        body.put("status", 422);
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
        body.put("errors", errors);
        return ResponseEntity.unprocessableEntity().body(body);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", ZonedDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
