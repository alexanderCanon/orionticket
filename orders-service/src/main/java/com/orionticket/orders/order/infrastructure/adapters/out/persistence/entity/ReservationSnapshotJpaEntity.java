package com.orionticket.orders.order.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reservation_snapshots")
@Data
@NoArgsConstructor
public class ReservationSnapshotJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID reservationId;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID seatId;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID batchId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal batchPrice;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID buyerId;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID eventId;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID dateId;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private Instant receivedAt;
}
