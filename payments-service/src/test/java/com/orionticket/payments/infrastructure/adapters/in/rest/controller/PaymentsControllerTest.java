package com.orionticket.payments.infrastructure.adapters.in.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orionticket.payments.application.port.in.InitiatePaymentUseCase;
import com.orionticket.payments.application.port.in.ProcessWebhookUseCase;
import com.orionticket.payments.domain.exception.PaymentNotFoundException;
import com.orionticket.payments.domain.model.Payment;
import com.orionticket.payments.domain.port.out.PaymentRepositoryPort;
import com.orionticket.payments.infrastructure.adapters.in.rest.GlobalExceptionHandler;
import com.orionticket.payments.infrastructure.adapters.in.rest.mapper.PaymentDtoMapper;
import com.orionticket.payments.infrastructure.security.AuthenticatedUserResolver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentsController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({PaymentDtoMapper.class, GlobalExceptionHandler.class})
class PaymentsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InitiatePaymentUseCase initiatePayment;

    @MockBean
    private ProcessWebhookUseCase processWebhook;

    @MockBean
    private PaymentRepositoryPort paymentRepository;

    @MockBean
    private StripeWebhookSignatureVerifier signatureVerifier;

    @MockBean
    private AuthenticatedUserResolver authenticatedUserResolver;

    @Test
    void postPaymentsReturnsCreatedPaymentResponse() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();
        Payment payment = payment(orderId, buyerId);
        when(authenticatedUserResolver.currentUserId()).thenReturn(buyerId);
        when(initiatePayment.initiate(eq(orderId), eq(buyerId), eq(Payment.PaymentMethod.CARD), eq("tok-card")))
                .thenReturn(payment);

        mockMvc.perform(post("/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "orderId", orderId,
                                "buyerId", buyerId,
                                "method", "CARD",
                                "paymentDetails", Map.of("gatewayToken", "tok-card")))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentId").value(payment.getPaymentId().toString()))
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.status").value("INITIATED"))
                .andExpect(jsonPath("$.method").value("CARD"));
    }

    @Test
    void postPaymentsRejectsInvalidPaymentMethod() throws Exception {
        mockMvc.perform(post("/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "orderId", UUID.randomUUID(),
                                "buyerId", UUID.randomUUID(),
                                "method", "CASH"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void postWebhookDelegatesToUseCaseAndAcknowledges() throws Exception {
        UUID paymentId = UUID.randomUUID();

        mockMvc.perform(post("/v1/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "paymentId", paymentId,
                                "gatewayReference", "gw-123",
                                "result", "AUTHORIZED"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acknowledged").value(true));

        verify(processWebhook).processWebhook(paymentId, "gw-123", "AUTHORIZED", null);
    }

    @Test
    void getPaymentReturnsPaymentWhenFound() throws Exception {
        Payment payment = payment(UUID.randomUUID(), UUID.randomUUID());
        when(paymentRepository.findById(payment.getPaymentId())).thenReturn(Optional.of(payment));

        mockMvc.perform(get("/v1/payments/{paymentId}", payment.getPaymentId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(payment.getPaymentId().toString()))
                .andExpect(jsonPath("$.status").value("INITIATED"));
    }

    @Test
    void getPaymentReturnsNotFoundWhenMissing() throws Exception {
        UUID paymentId = UUID.randomUUID();
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/v1/payments/{paymentId}", paymentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PAYMENT_NOT_FOUND"));
    }

    private static Payment payment(UUID orderId, UUID buyerId) {
        return Payment.initiate(
                orderId,
                buyerId,
                new BigDecimal("250.00"),
                new BigDecimal("20.00"),
                "GTQ",
                Payment.PaymentMethod.CARD,
                "pay-" + orderId);
    }
}
