package com.orionticket.ticketissuance.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class Ticket {

    private final UUID ticketId;
    private final UUID orderId;
    private final UUID buyerId;
    private final UUID eventId;
    private final UUID dateId;
    private final UUID seatId;
    private final TicketType type;
    private final String holderName;
    private final String qrCode;
    private final Instant qrExpiresAt;
    private final String accessPolicy;
    private final TicketStatus status;
    private final Instant deliveredAt;
    private final Instant issuedAt;
    private final Set<DeliveryChannel> deliveryChannels;

    public Ticket(
            UUID ticketId,
            UUID orderId,
            UUID buyerId,
            UUID eventId,
            UUID dateId,
            UUID seatId,
            TicketType type,
            String holderName,
            String qrCode,
            Instant qrExpiresAt,
            String accessPolicy,
            TicketStatus status,
            Instant deliveredAt,
            Instant issuedAt,
            Set<DeliveryChannel> deliveryChannels
    ) {
        this.ticketId = Objects.requireNonNull(ticketId, "ticketId is required");
        this.orderId = Objects.requireNonNull(orderId, "orderId is required");
        this.buyerId = Objects.requireNonNull(buyerId, "buyerId is required");
        this.eventId = Objects.requireNonNull(eventId, "eventId is required");
        this.dateId = Objects.requireNonNull(dateId, "dateId is required");
        this.type = Objects.requireNonNull(type, "type is required");
        this.holderName = requireText(holderName, "holderName is required");
        this.qrCode = requireText(qrCode, "qrCode is required");
        this.qrExpiresAt = Objects.requireNonNull(qrExpiresAt, "qrExpiresAt is required");
        this.accessPolicy = requireText(accessPolicy, "accessPolicy is required");
        this.status = Objects.requireNonNull(status, "status is required");
        this.issuedAt = Objects.requireNonNull(issuedAt, "issuedAt is required");
        this.seatId = seatId;
        this.deliveredAt = deliveredAt;
        this.deliveryChannels = Objects.requireNonNull(deliveryChannels, "deliveryChannels is required");

        if (type == TicketType.MAPPED && seatId == null) {
            throw new IllegalArgumentException("seatId is required for mapped tickets");
        }
        if (type == TicketType.GENERAL_ADMISSION && seatId != null) {
            throw new IllegalArgumentException("seatId must be null for general admission tickets");
        }
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    public UUID ticketId() {
        return ticketId;
    }

    public UUID orderId() {
        return orderId;
    }

    public UUID buyerId() {
        return buyerId;
    }

    public UUID eventId() {
        return eventId;
    }

    public UUID dateId() {
        return dateId;
    }

    public UUID seatId() {
        return seatId;
    }

    public TicketType type() {
        return type;
    }

    public String holderName() {
        return holderName;
    }

    public String qrCode() {
        return qrCode;
    }

    public Instant qrExpiresAt() {
        return qrExpiresAt;
    }

    public String accessPolicy() {
        return accessPolicy;
    }

    public TicketStatus status() {
        return status;
    }

    public Instant deliveredAt() {
        return deliveredAt;
    }

    public Instant issuedAt() {
        return issuedAt;
    }

    public Set<DeliveryChannel> deliveryChannels() {
        return deliveryChannels;
    }
}
