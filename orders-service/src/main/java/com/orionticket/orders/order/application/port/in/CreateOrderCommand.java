package com.orionticket.orders.order.application.port.in;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CreateOrderCommand {
    private UUID buyerId;
    private UUID eventId;
    private UUID dateId;
    private UUID reservationId;
    private String promotionCode;  // puede ser null
}
