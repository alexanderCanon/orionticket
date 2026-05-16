package com.orionticket.orders.order.domain.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

// Snapshot local de una reserva recibida desde seating-inventory via ReservationCreated.
// Permite crear órdenes sin llamadas síncronas a otro servicio (patrón de snapshot de evento).
@Data
@Builder
public class ReservationSnapshot {

    private UUID reservationId;
    private UUID seatId;
    private UUID batchId;
    private BigDecimal batchPrice;  // precio de la tanda al momento de la reserva
    private UUID buyerId;
    private UUID eventId;
    private UUID dateId;
    private Instant expiresAt;      // copiado del evento para detectar reservas vencidas
    private Instant receivedAt;

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
