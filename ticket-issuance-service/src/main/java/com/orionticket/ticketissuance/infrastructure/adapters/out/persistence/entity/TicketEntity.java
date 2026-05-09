package com.orionticket.ticketissuance.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tickets")
public class TicketEntity {

    @Id
    @Column(name = "ticket_id", nullable = false)
    private UUID ticketId;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "buyer_id", nullable = false)
    private UUID buyerId;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "date_id", nullable = false)
    private UUID dateId;

    @Column(name = "seat_id")
    private UUID seatId;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "holder_name", nullable = false)
    private String holderName;

    @Column(name = "qr_code", nullable = false)
    private String qrCode;

    @Column(name = "qr_expires_at", nullable = false)
    private Instant qrExpiresAt;

    @Column(name = "access_policy", nullable = false)
    private String accessPolicy;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    public UUID getTicketId() {
        return ticketId;
    }

    public void setTicketId(UUID ticketId) {
        this.ticketId = ticketId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(UUID buyerId) {
        this.buyerId = buyerId;
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public UUID getDateId() {
        return dateId;
    }

    public void setDateId(UUID dateId) {
        this.dateId = dateId;
    }

    public UUID getSeatId() {
        return seatId;
    }

    public void setSeatId(UUID seatId) {
        this.seatId = seatId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHolderName() {
        return holderName;
    }

    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public Instant getQrExpiresAt() {
        return qrExpiresAt;
    }

    public void setQrExpiresAt(Instant qrExpiresAt) {
        this.qrExpiresAt = qrExpiresAt;
    }

    public String getAccessPolicy() {
        return accessPolicy;
    }

    public void setAccessPolicy(String accessPolicy) {
        this.accessPolicy = accessPolicy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(Instant deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
    }
}
