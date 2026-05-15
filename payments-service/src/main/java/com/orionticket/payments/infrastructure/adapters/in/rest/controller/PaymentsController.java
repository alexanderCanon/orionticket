package com.orionticket.payments.infrastructure.adapters.in.rest.controller;

import com.orionticket.payments.application.port.in.InitiatePaymentUseCase;
import com.orionticket.payments.application.port.in.ProcessWebhookUseCase;
import com.orionticket.payments.domain.exception.PaymentNotFoundException;
import com.orionticket.payments.domain.model.Payment;
import com.orionticket.payments.infrastructure.adapters.in.rest.dto.InitiatePaymentRequest;
import com.orionticket.payments.infrastructure.adapters.in.rest.dto.PaymentResponse;
import com.orionticket.payments.infrastructure.adapters.in.rest.dto.WebhookRequest;
import com.orionticket.payments.infrastructure.adapters.in.rest.mapper.PaymentDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * REST controller for payment operations.
 * All business logic is delegated to use case ports — no logic in this class.
 */
@RestController
@RequestMapping("/v1/payments")
@Tag(name = "Payments", description = "Payment initiation, webhook processing, and payment status endpoints")
public class PaymentsController {

    private final InitiatePaymentUseCase initiatePayment;
    private final ProcessWebhookUseCase processWebhook;
    private final com.orionticket.payments.domain.port.out.PaymentRepositoryPort paymentRepository;
    private final PaymentDtoMapper mapper;

    public PaymentsController(InitiatePaymentUseCase initiatePayment,
                              ProcessWebhookUseCase processWebhook,
                              com.orionticket.payments.domain.port.out.PaymentRepositoryPort paymentRepository,
                              PaymentDtoMapper mapper) {
        this.initiatePayment = initiatePayment;
        this.processWebhook = processWebhook;
        this.paymentRepository = paymentRepository;
        this.mapper = mapper;
    }

    /**
     * POST /v1/payments
     * Initiate payment for an Order. (UC-PA-01)
     */
    @Operation(summary = "Initiate payment", description = "Creates or returns the idempotent payment for an order.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Payment initiated or existing payment returned"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Order is not payable"),
            @ApiResponse(responseCode = "502", description = "Payment gateway unavailable")
    })
    @PostMapping
    public ResponseEntity<PaymentResponse> initiatePayment(
            @Valid @RequestBody InitiatePaymentRequest request) {

        Payment.PaymentMethod method = Payment.PaymentMethod.valueOf(request.getMethod().toUpperCase());
        String gatewayToken = request.getPaymentDetails() != null
                ? request.getPaymentDetails().getGatewayToken()
                : null;

        Payment payment = initiatePayment.initiate(
                request.getOrderId(),
                request.getBuyerId(),
                method,
                gatewayToken
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(payment));
    }

    /**
     * POST /v1/payments/webhook
     * Receive authorization or failure result from the payment gateway. (UC-PA-01, UC-PA-02)
     * Permitted without authentication until gateway signature validation is implemented.
     */
    @Operation(summary = "Process payment webhook", description = "Receives final authorization or failure result from the payment gateway.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Webhook acknowledged"),
            @ApiResponse(responseCode = "400", description = "Invalid webhook payload"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Boolean>> processWebhook(
            @Valid @RequestBody WebhookRequest request) {

        processWebhook.processWebhook(
                request.getPaymentId(),
                request.getGatewayReference(),
                request.getResult(),
                request.getFailureReason()
        );

        return ResponseEntity.ok(Map.of("acknowledged", true));
    }

    /**
     * GET /v1/payments/{paymentId}
     * Retrieve payment status. (UC-PA-01 status check)
     */
    @Operation(summary = "Get payment", description = "Returns payment status and financial summary by payment ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment found"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found. paymentId=" + paymentId));
        return ResponseEntity.ok(mapper.toResponse(payment));
    }
}
