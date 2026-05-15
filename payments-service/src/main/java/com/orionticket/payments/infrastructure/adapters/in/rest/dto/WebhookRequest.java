package com.orionticket.payments.infrastructure.adapters.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

/**
 * Request DTO for the payment gateway webhook (POST /v1/payments/webhook).
 * Fields are validated to prevent NPEs deep in the service layer.
 */
public class WebhookRequest {

    @NotNull(message = "paymentId is required")
    private UUID paymentId;

    @NotBlank(message = "gatewayReference is required")
    private String gatewayReference;

    @NotBlank(message = "result is required")
    @Pattern(regexp = "AUTHORIZED|FAILED", message = "result must be AUTHORIZED or FAILED")
    private String result;

    private String failureReason;  // null when AUTHORIZED

    public UUID getPaymentId() { return paymentId; }
    public void setPaymentId(UUID paymentId) { this.paymentId = paymentId; }

    public String getGatewayReference() { return gatewayReference; }
    public void setGatewayReference(String gatewayReference) { this.gatewayReference = gatewayReference; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
}