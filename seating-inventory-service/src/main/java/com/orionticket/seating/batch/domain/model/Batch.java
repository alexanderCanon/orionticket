package com.orionticket.seating.batch.domain.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

// Dominio puro: sin Spring, sin JPA, sin RabbitMQ. Solo POJOs con lógica de negocio.
// Esta es la entidad de dominio; BatchJpaEntity es el mapeo a la tabla (capa de infraestructura).
@Data
@Builder
public class Batch {

    private UUID batchId;
    private UUID eventId;
    private UUID dateId;
    private String name;
    private BigDecimal price;
    private String currency;
    private int capacity;
    private int sold;
    private String status; // SCHEDULED | ACTIVE | EXHAUSTED | EXPIRED
    private ZonedDateTime scheduledStartAt;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    // Factory method: encapsula la lógica de creación para que ningún caller
    // pueda construir un Batch en estado inválido (por ejemplo, con sold > 0 al crear).
    // Si scheduledStartAt ya pasó, la tanda empieza ACTIVE de inmediato.
    public static Batch create(UUID eventId, UUID dateId, String name,
                               BigDecimal price, String currency,
                               int capacity, ZonedDateTime scheduledStartAt) {
        String initialStatus = scheduledStartAt.isBefore(ZonedDateTime.now())
                ? "ACTIVE" : "SCHEDULED";
        ZonedDateTime now = ZonedDateTime.now();
        return Batch.builder()
                .batchId(UUID.randomUUID())
                .eventId(eventId)
                .dateId(dateId)
                .name(name)
                .price(price)
                .currency(currency)
                .capacity(capacity)
                .sold(0)
                .status(initialStatus)
                .scheduledStartAt(scheduledStartAt)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public boolean isActive()    { return "ACTIVE".equals(this.status); }
    public boolean isExhausted() { return this.sold >= this.capacity; }

    // Incrementa sold y transiciona a EXHAUSTED si se llena el cupo.
    // Llamado dentro de @Transactional en ReservationService para garantizar atomicidad.
    public void incrementSold() {
        if (isExhausted()) {
            throw new IllegalStateException("Batch is already exhausted");
        }
        this.sold++;
        if (this.sold >= this.capacity) {
            this.status = "EXHAUSTED";
        }
        this.updatedAt = ZonedDateTime.now();
    }

    // Decrementa sold al liberar o expirar una reserva.
    // Math.max(0, ...) evita valores negativos si hay inconsistencia de datos.
    public void decrementSold() {
        this.sold = Math.max(0, this.sold - 1);
        if ("EXHAUSTED".equals(this.status) && this.sold < this.capacity) {
            this.status = "ACTIVE";
        }
        this.updatedAt = ZonedDateTime.now();
    }

    public void activate() {
        this.status = "ACTIVE";
        this.updatedAt = ZonedDateTime.now();
    }
}
