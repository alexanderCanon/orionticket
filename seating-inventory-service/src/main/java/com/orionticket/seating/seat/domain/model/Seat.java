package com.orionticket.seating.seat.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
public class Seat {

    private UUID seatId;
    private UUID eventId;
    private UUID dateId;
    private UUID batchId;
    private String zone;
    private String section;
    private String rowLabel;
    private String seatNumber;
    private String type;          // MAPPED | GENERAL_ADMISSION
    private String status;        // AVAILABLE | RESERVED | SOLD | BLOCKED
    private String accessPolicy;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    // Factory para asiento numerado (MAPPED): zona, sección, fila y número requeridos.
    public static Seat createMapped(UUID eventId, UUID dateId, UUID batchId,
                                    String zone, String section,
                                    String rowLabel, String seatNumber,
                                    String accessPolicy) {
        ZonedDateTime now = ZonedDateTime.now();
        return Seat.builder()
                .seatId(UUID.randomUUID())
                .eventId(eventId)
                .dateId(dateId)
                .batchId(batchId)
                .zone(zone)
                .section(section)
                .rowLabel(rowLabel)
                .seatNumber(seatNumber)
                .type("MAPPED")
                .status("AVAILABLE")
                .accessPolicy(accessPolicy)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    // Factory para asiento de admisión general (GA): sin zona/sección/fila/número.
    // Estos asientos son genéricos; el comprador no elige un lugar físico específico.
    public static Seat createGeneralAdmission(UUID eventId, UUID dateId, UUID batchId,
                                              String accessPolicy) {
        ZonedDateTime now = ZonedDateTime.now();
        return Seat.builder()
                .seatId(UUID.randomUUID())
                .eventId(eventId)
                .dateId(dateId)
                .batchId(batchId)
                .type("GENERAL_ADMISSION")
                .status("AVAILABLE")
                .accessPolicy(accessPolicy)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public boolean isAvailable() { return "AVAILABLE".equals(this.status); }

    public void reserve() {
        this.status = "RESERVED";
        this.updatedAt = ZonedDateTime.now();
    }

    public void release() {
        this.status = "AVAILABLE";
        this.updatedAt = ZonedDateTime.now();
    }

    public void sell() {
        this.status = "SOLD";
        this.updatedAt = ZonedDateTime.now();
    }
}
