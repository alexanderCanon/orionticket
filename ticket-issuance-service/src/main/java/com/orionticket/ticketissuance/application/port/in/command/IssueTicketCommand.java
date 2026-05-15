package com.orionticket.ticketissuance.application.port.in.command;

import com.orionticket.ticketissuance.domain.model.DeliveryChannel;
import com.orionticket.ticketissuance.domain.model.TicketType;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record IssueTicketCommand(
        UUID orderId,
        UUID buyerId,
        UUID eventId,
        UUID dateId,
        UUID seatId, // Nullable for general admission
        TicketType type,
        String holderName,
        String qrCode,
        Instant qrExpiresAt,
        String accessPolicy,
        Set<DeliveryChannel> deliveryChannels
) {
}
