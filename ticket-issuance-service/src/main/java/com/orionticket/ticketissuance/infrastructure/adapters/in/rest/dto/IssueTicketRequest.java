package com.orionticket.ticketissuance.infrastructure.adapters.in.rest.dto;

import com.orionticket.ticketissuance.domain.model.DeliveryChannel;
import com.orionticket.ticketissuance.domain.model.TicketType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record IssueTicketRequest(
        @NotNull UUID orderId,
        @NotNull UUID buyerId,
        @NotNull UUID eventId,
        @NotNull UUID dateId,
        UUID seatId, // Nullable for general admission
        @NotNull TicketType type,
        @NotBlank String holderName,
        @NotBlank String qrCode,
        @NotNull Instant qrExpiresAt,
        @NotBlank String accessPolicy,
        @NotNull Set<DeliveryChannel> deliveryChannels
) {
}
