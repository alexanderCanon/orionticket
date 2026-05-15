package com.orionticket.seating.reservation.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "reservations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationJpaEntity {

    @Id
    @Column(name = "reservation_id", columnDefinition = "uuid")
    private UUID reservationId;

    @Column(name = "seat_id", nullable = false, columnDefinition = "uuid")
    private UUID seatId;

    @Column(name = "buyer_id", nullable = false, columnDefinition = "uuid")
    private UUID buyerId;

    @Column(name = "event_id", nullable = false, columnDefinition = "uuid")
    private UUID eventId;

    @Column(name = "date_id", nullable = false, columnDefinition = "uuid")
    private UUID dateId;

    @Column(name = "batch_id", nullable = false, columnDefinition = "uuid")
    private UUID batchId;

    @Column(name = "expires_at", nullable = false)
    private ZonedDateTime expiresAt;

    @Column(nullable = false, length = 30)
    private String status;

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
