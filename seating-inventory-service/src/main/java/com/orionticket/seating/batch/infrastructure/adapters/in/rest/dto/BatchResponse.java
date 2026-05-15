package com.orionticket.seating.batch.infrastructure.adapters.in.rest.dto;

import com.orionticket.seating.batch.domain.model.Batch;
import lombok.Data;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
public class BatchResponse {
    private UUID batchId;
    private UUID eventId;
    private UUID dateId;
    private String name;
    private BigDecimal price;
    private String currency;
    private int capacity;
    private int sold;
    private String status;
    private ZonedDateTime scheduledStartAt;
    private ZonedDateTime createdAt;

    public static BatchResponse from(Batch batch) {
        BatchResponse r = new BatchResponse();
        r.batchId = batch.getBatchId();
        r.eventId = batch.getEventId();
        r.dateId = batch.getDateId();
        r.name = batch.getName();
        r.price = batch.getPrice();
        r.currency = batch.getCurrency();
        r.capacity = batch.getCapacity();
        r.sold = batch.getSold();
        r.status = batch.getStatus();
        r.scheduledStartAt = batch.getScheduledStartAt();
        r.createdAt = batch.getCreatedAt();
        return r;
    }
}
