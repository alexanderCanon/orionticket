package com.orionticket.seating.seat.infrastructure.adapters.in.rest.dto;

import com.orionticket.seating.seat.domain.model.Seat;
import lombok.Data;

import java.util.UUID;

@Data
public class SeatResponse {
    private UUID seatId;
    private UUID batchId;
    private String zone;
    private String section;
    private String row;
    private String seatNumber;
    private String type;
    private String status;
    private String accessPolicy;

    public static SeatResponse from(Seat seat) {
        SeatResponse r = new SeatResponse();
        r.seatId = seat.getSeatId();
        r.batchId = seat.getBatchId();
        r.zone = seat.getZone();
        r.section = seat.getSection();
        r.row = seat.getRowLabel();
        r.seatNumber = seat.getSeatNumber();
        r.type = seat.getType();
        r.status = seat.getStatus();
        r.accessPolicy = seat.getAccessPolicy();
        return r;
    }
}
