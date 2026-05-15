package com.orionticket.seating.reservation.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
public class Reservation {

    private UUID reservationId;
    private UUID seatId;
    private UUID buyerId;
    private UUID eventId;
    private UUID dateId;
    private UUID batchId;
    private ZonedDateTime expiresAt;
    private String status; // ACTIVE | EXPIRED | RELEASED
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    // El hold dura 10 minutos. Si el comprador no paga en ese tiempo,
    // el job de expiración libera el asiento automáticamente.
    private static final int EXPIRATION_MINUTES = 10;

    public static Reservation create(UUID seatId, UUID buyerId,
                                     UUID eventId, UUID dateId, UUID batchId) {
        ZonedDateTime now = ZonedDateTime.now();
        return Reservation.builder()
                .reservationId(UUID.randomUUID())
                .seatId(seatId)
                .buyerId(buyerId)
                .eventId(eventId)
                .dateId(dateId)
                .batchId(batchId)
                .expiresAt(now.plusMinutes(EXPIRATION_MINUTES))
                .status("ACTIVE")
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public boolean isActive()  { return "ACTIVE".equals(this.status); }
    public boolean isExpired() { return ZonedDateTime.now().isAfter(this.expiresAt); }

    public void expire() {
        this.status = "EXPIRED";
        this.updatedAt = ZonedDateTime.now();
    }

    public void release() {
        this.status = "RELEASED";
        this.updatedAt = ZonedDateTime.now();
    }
}
