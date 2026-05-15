package com.orionticket.ticketissuance.infrastructure.adapters.in.rest.dto;

import com.orionticket.ticketissuance.domain.model.DeliveryChannel;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record TicketResponse(
        UUID ticketId,
        UUID orderId,
        UUID buyerId,
        UUID eventId,
        UUID dateId,
        UUID seatId,
        String type,
        String holderName,
        String qrCode,
        Instant qrExpiresAt,
        String accessPolicy,
        String status,
        Instant issuedAt,
        Set<DeliveryChannel> deliveryChannels
) {
}
