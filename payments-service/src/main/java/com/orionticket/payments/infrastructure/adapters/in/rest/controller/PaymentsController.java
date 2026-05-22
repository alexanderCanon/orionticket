package com.orionticket.payments.infrastructure.adapters.in.rest.controller;

import com.orionticket.payments.application.port.in.InitiatePaymentUseCase;
import com.orionticket.payments.application.port.in.ProcessWebhookUseCase;
import com.orionticket.payments.domain.exception.PaymentNotFoundException;
import com.orionticket.payments.domain.model.Payment;
import com.orionticket.payments.infrastructure.adapters.in.rest.dto.InitiatePaymentRequest;
import com.orionticket.payments.infrastructure.adapters.in.rest.dto.PaymentResponse;
import com.orionticket.payments.infrastructure.adapters.in.rest.dto.WebhookRequest;
import com.orionticket.payments.infrastructure.adapters.in.rest.mapper.PaymentDtoMapper;
import com.orionticket.payments.infrastructure.security.AuthenticatedUserResolver;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.exception.SignatureVerificationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    private static final Logger log = LoggerFactory.getLogger(PaymentsController.class);

    private final InitiatePaymentUseCase initiatePayment;
    private final ProcessWebhookUseCase processWebhook;
    private final com.orionticket.payments.domain.port.out.PaymentRepositoryPort paymentRepository;
    private final PaymentDtoMapper mapper;
    private final StripeWebhookSignatureVerifier signatureVerifier;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    public PaymentsController(InitiatePaymentUseCase initiatePayment,
                              ProcessWebhookUseCase processWebhook,
                              com.orionticket.payments.domain.port.out.PaymentRepositoryPort paymentRepository,
                              PaymentDtoMapper mapper,
                              StripeWebhookSignatureVerifier signatureVerifier,
                              AuthenticatedUserResolver authenticatedUserResolver) {
        this.initiatePayment = initiatePayment;
        this.processWebhook = processWebhook;
        this.paymentRepository = paymentRepository;
        this.mapper = mapper;
        this.signatureVerifier = signatureVerifier;
        this.authenticatedUserResolver = authenticatedUserResolver;
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
    @PreAuthorize("hasRole('BUYER') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<PaymentResponse> initiatePayment(
            @Valid @RequestBody InitiatePaymentRequest request) {

        Payment.PaymentMethod method = Payment.PaymentMethod.valueOf(request.getMethod().toUpperCase());
        String gatewayToken = request.getPaymentDetails() != null
                ? request.getPaymentDetails().getGatewayToken()
                : null;

        Payment payment = initiatePayment.initiate(
                request.getOrderId(),
                authenticatedUserResolver.currentUserId(),
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
     * POST /v1/payments/stripe/webhook
     * Receive raw webhook from Stripe, verify signature and process.
     */
    @Operation(summary = "Process Stripe webhook", description = "Receives raw Stripe payment event webhook and verifies its signature.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Webhook acknowledged"),
            @ApiResponse(responseCode = "400", description = "Invalid webhook payload or signature"),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    @PostMapping("/stripe/webhook")
    public ResponseEntity<Map<String, Boolean>> processStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        log.info("Received Stripe webhook request");
        Event event;
        try {
            event = signatureVerifier.verifyAndConstruct(payload, sigHeader);
        } catch (SignatureVerificationException e) {
            log.error("Stripe signature verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("signatureVerified", false));
        }

        if ("payment_intent.succeeded".equals(event.getType()) || "payment_intent.payment_failed".equals(event.getType())) {
            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
            if (paymentIntent == null) {
                log.error("Deserialization of Stripe PaymentIntent failed for event: {}", event.getId());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            // Extract metadata
            Map<String, String> metadata = paymentIntent.getMetadata();
            String paymentIdStr = metadata != null ? metadata.get("paymentId") : null;
            UUID paymentId;

            if (paymentIdStr != null && !paymentIdStr.isBlank()) {
                paymentId = UUID.fromString(paymentIdStr);
            } else {
                // Fallback to searching by gatewayReference
                String paymentIntentId = paymentIntent.getId();
                Payment payment = paymentRepository.findByGatewayReference(paymentIntentId)
                        .orElseThrow(() -> new PaymentNotFoundException(
                                "Payment not found for Stripe PaymentIntent ID: " + paymentIntentId));
                paymentId = payment.getPaymentId();
            }

            String result = "payment_intent.succeeded".equals(event.getType()) ? "AUTHORIZED" : "FAILED";
            String failureReason = null;
            if ("FAILED".equals(result)) {
                failureReason = paymentIntent.getLastPaymentError() != null
                        ? paymentIntent.getLastPaymentError().getMessage()
                        : "Stripe payment failed";
            }

            processWebhook.processWebhook(
                    paymentId,
                    paymentIntent.getId(),
                    result,
                    failureReason
            );
        } else {
            log.info("Unhandled Stripe event type: {}", event.getType());
        }

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
    @PreAuthorize("hasRole('BUYER') or hasRole('FINANCE') or hasRole('PLATFORM_OPERATOR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found. paymentId=" + paymentId));
        authenticatedUserResolver.requirePaymentReadAccess(payment.getBuyerId());
        return ResponseEntity.ok(mapper.toResponse(payment));
    }
}
