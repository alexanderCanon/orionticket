package com.orionticket.seating.seat.infrastructure.adapters.out.persistence.entity;

import com.orionticket.seating.batch.infrastructure.adapters.out.persistence.entity.BatchJpaEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "seats")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatJpaEntity {

    @Id
    @Column(name = "seat_id", columnDefinition = "uuid")
    private UUID seatId;

    @Column(name = "event_id", nullable = false, columnDefinition = "uuid")
    private UUID eventId;

    @Column(name = "date_id", nullable = false, columnDefinition = "uuid")
    private UUID dateId;

    // @ManyToOne con JOIN FETCH en las queries de disponibilidad para obtener
    // precio y nombre del batch sin N+1 queries separadas.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private BatchJpaEntity batch;

    @Column(length = 100)
    private String zone;

    @Column(length = 100)
    private String section;

    @Column(name = "row_label", length = 50)
    private String rowLabel;

    @Column(name = "seat_number", length = 50)
    private String seatNumber;

    @Column(nullable = false, length = 30)
    private String type;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "access_policy", length = 255)
    private String accessPolicy;

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
