package com.orionticket.payments.infrastructure.adapters.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

/**
 * Request DTO for POST /v1/payments (initiate payment).
 * Structural validation only — business invariants are enforced in the domain layer.
 */
public class InitiatePaymentRequest {

    @NotNull(message = "orderId is required")
    private UUID orderId;

    @NotNull(message = "buyerId is required")
    private UUID buyerId;

    @NotBlank(message = "method is required")
    @Pattern(regexp = "CARD|TRANSFER|card|transfer",
             message = "method must be CARD or TRANSFER")
    private String method;

    private PaymentDetails paymentDetails;

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }

    public UUID getBuyerId() { return buyerId; }
    public void setBuyerId(UUID buyerId) { this.buyerId = buyerId; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public PaymentDetails getPaymentDetails() { return paymentDetails; }
    public void setPaymentDetails(PaymentDetails paymentDetails) { this.paymentDetails = paymentDetails; }

    public static class PaymentDetails {
        private String gatewayToken;
        public String getGatewayToken() { return gatewayToken; }
        public void setGatewayToken(String gatewayToken) { this.gatewayToken = gatewayToken; }
    }
}