package com.orionticket.payments.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Local projection of OrderCreated events from the Orders service.
 * Populated by OrderCreatedConsumer; queried by OrderProjectionAdapter.
 */
@Entity
@Table(name = "order_projections")
public class OrderProjectionEntity {

    @Id
    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "buyer_id", nullable = false)
    private UUID buyerId;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "date_id", nullable = false)
    private UUID dateId;

    @Column(name = "total", nullable = false, precision = 19, scale = 4)
    private BigDecimal total;

    @Column(name = "service_fee", nullable = false, precision = 19, scale = 4)
    private BigDecimal serviceFee;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }

    public UUID getBuyerId() { return buyerId; }
    public void setBuyerId(UUID buyerId) { this.buyerId = buyerId; }

    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }

    public UUID getDateId() { return dateId; }
    public void setDateId(UUID dateId) { this.dateId = dateId; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public BigDecimal getServiceFee() { return serviceFee; }
    public void setServiceFee(BigDecimal serviceFee) { this.serviceFee = serviceFee; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getReceivedAt() { return receivedAt; }
    public void setReceivedAt(Instant receivedAt) { this.receivedAt = receivedAt; }
}
