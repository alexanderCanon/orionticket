package com.orionticket.orders.order.infrastructure.adapters.in.rest.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class LineItemResponse {
    private UUID lineItemId;
    private UUID seatId;
    private BigDecimal batchPrice;
    private int quantity;
}
