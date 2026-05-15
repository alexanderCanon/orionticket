package com.orionticket.seating.reservation.infrastructure.adapters.in.rest.dto;

import com.orionticket.seating.reservation.domain.model.Reservation;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
public class ReservationResponse {
    private UUID reservationId;
    private UUID seatId;
    private UUID buyerId;
    private UUID eventId;
    private UUID dateId;
    private UUID batchId;
    private ZonedDateTime expiresAt;
    private String status;
    private ZonedDateTime createdAt;

    public static ReservationResponse from(Reservation reservation) {
        ReservationResponse r = new ReservationResponse();
        r.reservationId = reservation.getReservationId();
        r.seatId = reservation.getSeatId();
        r.buyerId = reservation.getBuyerId();
        r.eventId = reservation.getEventId();
        r.dateId = reservation.getDateId();
        r.batchId = reservation.getBatchId();
        r.expiresAt = reservation.getExpiresAt();
        r.status = reservation.getStatus();
        r.createdAt = reservation.getCreatedAt();
        return r;
    }
}
