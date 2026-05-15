package com.orionticket.seating.batch.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

// Entidad JPA: mapeo entre el objeto Java y la tabla `batches` en PostgreSQL.
// Separada del modelo de dominio Batch.java para cumplir la regla de Hexagonal:
// el dominio no conoce JPA; la infraestructura sí.
@Entity
@Table(name = "batches")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchJpaEntity {

    @Id
    @Column(name = "batch_id", columnDefinition = "uuid")
    private UUID batchId;

    @Column(name = "event_id", nullable = false, columnDefinition = "uuid")
    private UUID eventId;

    @Column(name = "date_id", nullable = false, columnDefinition = "uuid")
    private UUID dateId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private int sold;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(name = "scheduled_start_at", nullable = false)
    private ZonedDateTime scheduledStartAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @PrePersist
    void onCreate() {
        ZonedDateTime now = ZonedDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = ZonedDateTime.now();
    }
}
