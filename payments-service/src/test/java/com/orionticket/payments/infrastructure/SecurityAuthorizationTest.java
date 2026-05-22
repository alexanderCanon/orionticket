package com.orionticket.payments.infrastructure;

import com.orionticket.payments.application.port.in.InitiatePaymentUseCase;
import com.orionticket.payments.application.port.in.ManagePayoutsUseCase;
import com.orionticket.payments.application.port.in.ProcessWebhookUseCase;
import com.orionticket.payments.domain.model.Payment;
import com.orionticket.payments.domain.model.Payout;
import com.orionticket.payments.domain.port.out.PaymentRepositoryPort;
import com.orionticket.payments.infrastructure.adapters.in.rest.GlobalExceptionHandler;
import com.orionticket.payments.infrastructure.adapters.in.rest.controller.PaymentsController;
import com.orionticket.payments.infrastructure.adapters.in.rest.controller.PayoutsController;
import com.orionticket.payments.infrastructure.adapters.in.rest.controller.StripeWebhookSignatureVerifier;
import com.orionticket.payments.infrastructure.adapters.in.rest.mapper.PaymentDtoMapper;
import com.orionticket.payments.infrastructure.config.SecurityConfig;
import com.orionticket.payments.infrastructure.security.AuthenticatedUserResolver;
import com.orionticket.payments.infrastructure.security.JwtAuthoritiesConverter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        PaymentsController.class,
        PayoutsController.class
})
@Import({
        SecurityConfig.class,
        JwtAuthoritiesConverter.class,
        AuthenticatedUserResolver.class,
        PaymentDtoMapper.class,
        GlobalExceptionHandler.class
})
@TestPropertySource(properties = {
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://identity-service:8081/.well-known/jwks.json",
        "jwt.issuer=orionticket-identity"
})
class SecurityAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InitiatePaymentUseCase initiatePayment;

    @MockBean
    private ProcessWebhookUseCase processWebhook;

    @MockBean
    private ManagePayoutsUseCase managePayouts;

    @MockBean
    private PaymentRepositoryPort paymentRepository;

    @MockBean
    private StripeWebhookSignatureVerifier signatureVerifier;

    @Test
    void initiatePaymentWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentRequest(UUID.randomUUID(), UUID.randomUUID())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void buyerInitiatesPaymentWithBuyerIdFromToken() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();
        UUID forgedBuyerId = UUID.randomUUID();
        Payment payment = payment(orderId, buyerId);
        when(initiatePayment.initiate(eq(orderId), eq(buyerId), eq(Payment.PaymentMethod.CARD), eq("tok-card")))
                .thenReturn(payment);

        mockMvc.perform(post("/v1/payments")
                        .with(jwt().jwt(jwt -> jwt
                                        .subject(buyerId.toString())
                                        .claim("role", "BUYER"))
                                .authorities(new SimpleGrantedAuthority("ROLE_BUYER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentRequest(orderId, forgedBuyerId)))
                .andExpect(status().isCreated());

        verify(initiatePayment).initiate(orderId, buyerId, Payment.PaymentMethod.CARD, "tok-card");
    }

    @Test
    void genericWebhookRemainsPublic() throws Exception {
        UUID paymentId = UUID.randomUUID();

        mockMvc.perform(post("/v1/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "paymentId": "%s",
                                  "gatewayReference": "gw-123",
                                  "result": "AUTHORIZED"
                                }
                                """.formatted(paymentId)))
                .andExpect(status().isOk());

        verify(processWebhook).processWebhook(paymentId, "gw-123", "AUTHORIZED", null);
    }

    @Test
    void buyerCannotReadAnotherBuyerPayment() throws Exception {
        UUID paymentId = UUID.randomUUID();
        UUID tokenBuyerId = UUID.randomUUID();
        Payment payment = payment(UUID.randomUUID(), UUID.randomUUID());
        payment.setPaymentId(paymentId);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        mockMvc.perform(get("/v1/payments/" + paymentId)
                        .with(jwt().jwt(jwt -> jwt
                                        .subject(tokenBuyerId.toString())
                                        .claim("role", "BUYER"))
                                .authorities(new SimpleGrantedAuthority("ROLE_BUYER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void financeCanReadAnyPayment() throws Exception {
        UUID paymentId = UUID.randomUUID();
        Payment payment = payment(UUID.randomUUID(), UUID.randomUUID());
        payment.setPaymentId(paymentId);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        mockMvc.perform(get("/v1/payments/" + paymentId)
                        .with(jwt().jwt(jwt -> jwt
                                        .subject(UUID.randomUUID().toString())
                                        .claim("role", "FINANCE"))
                                .authorities(new SimpleGrantedAuthority("ROLE_FINANCE"))))
                .andExpect(status().isOk());
    }

    @Test
    void organizerPayoutListIsScopedToTokenOrganizerId() throws Exception {
        UUID organizerId = UUID.randomUUID();
        UUID forgedOrganizerId = UUID.randomUUID();
        when(managePayouts.listPayouts(eq(organizerId), eq(null), eq(0), eq(20)))
                .thenReturn(List.of());

        mockMvc.perform(get("/v1/payouts")
                        .param("organizerId", forgedOrganizerId.toString())
                        .with(jwt().jwt(jwt -> jwt
                                        .subject(UUID.randomUUID().toString())
                                        .claim("role", "ORGANIZER")
                                        .claim("organizerId", organizerId.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_ORGANIZER"))))
                .andExpect(status().isOk());

        verify(managePayouts).listPayouts(organizerId, null, 0, 20);
    }

    @Test
    void buyerCannotListPayouts() throws Exception {
        mockMvc.perform(get("/v1/payouts")
                        .with(jwt().jwt(jwt -> jwt
                                        .subject(UUID.randomUUID().toString())
                                        .claim("role", "BUYER"))
                                .authorities(new SimpleGrantedAuthority("ROLE_BUYER"))))
                .andExpect(status().isForbidden());
    }

    private static String paymentRequest(UUID orderId, UUID buyerId) {
        return """
                {
                  "orderId": "%s",
                  "buyerId": "%s",
                  "method": "CARD",
                  "paymentDetails": {
                    "gatewayToken": "tok-card"
                  }
                }
                """.formatted(orderId, buyerId);
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

    @SuppressWarnings("unused")
    private static Payout payout(UUID organizerId) {
        return new Payout(
                UUID.randomUUID(),
                organizerId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("250.00"),
                new BigDecimal("20.00"),
                new BigDecimal("230.00"),
                Payout.PayoutStatus.PENDING,
                0,
                Instant.now(),
                null);
    }
}
