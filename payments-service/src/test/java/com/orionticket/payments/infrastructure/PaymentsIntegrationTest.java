package com.orionticket.payments.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orionticket.payments.PaymentsServiceApplication;
import com.orionticket.payments.domain.model.Payment;
import com.orionticket.payments.domain.port.out.PaymentRepositoryPort;
import com.orionticket.payments.infrastructure.adapters.in.messaging.OrderCreatedConsumer.OrderCreatedMessage;
import com.orionticket.payments.infrastructure.adapters.in.messaging.OrderCreatedConsumer.OrderCreatedMessage.Payload;
import com.orionticket.payments.infrastructure.adapters.in.rest.controller.StripeWebhookSignatureVerifier;
import com.orionticket.payments.infrastructure.adapters.out.persistence.repository.SpringDataOrderProjectionRepository;
import com.orionticket.payments.infrastructure.config.TestcontainersConfiguration;
import com.stripe.StripeClient;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.service.PaymentIntentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = PaymentsServiceApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.rabbitmq.listener.simple.auto-startup=true"
)
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
public class PaymentsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentRepositoryPort paymentRepository;

    @Autowired
    private SpringDataOrderProjectionRepository orderProjectionRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AmqpAdmin amqpAdmin;

    @MockBean
    private StripeClient stripeClient;

    @MockBean
    private StripeWebhookSignatureVerifier signatureVerifier;

    private PaymentIntentService paymentIntentService;

    @BeforeEach
    void setUp() {
        paymentIntentService = mock(PaymentIntentService.class);
        when(stripeClient.paymentIntents()).thenReturn(paymentIntentService);
    }

    @Test
    void testEndToEndPaymentFlow() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID dateId = UUID.randomUUID();

        // 1. Enforce JWT: Call /v1/payments without Authorization header -> should be 403 / 401
        mockMvc.perform(post("/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "orderId", orderId,
                        "buyerId", buyerId,
                        "method", "CARD",
                        "paymentDetails", Map.of("gatewayToken", "tok_visa")
        ))))
                .andExpect(status().isUnauthorized());

        // 2. Publish OrderCreated message to RabbitMQ orders exchange
        Payload payload = new Payload(
                orderId,
                buyerId,
                eventId,
                dateId,
                new BigDecimal("150.00"),
                new BigDecimal("15.00"),
                "GTQ",
                "CREATED"
        );
        OrderCreatedMessage message = new OrderCreatedMessage(
                "OrderCreated",
                UUID.randomUUID().toString(),
                Instant.now().toString(),
                payload
        );

        rabbitTemplate.convertAndSend("orders.events", "order.created", message);

        // Wait for OrderCreated consumer to process the message and save to projection
        boolean projectionCreated = false;
        for (int i = 0; i < 50; i++) {
            if (orderProjectionRepository.existsById(orderId)) {
                projectionCreated = true;
                break;
            }
            Thread.sleep(100);
        }
        assertThat(projectionCreated).isTrue();

        // 3. Mock Stripe PaymentIntent creation
        PaymentIntent mockIntent = mock(PaymentIntent.class);
        when(mockIntent.getId()).thenReturn("pi_test_123");
        when(mockIntent.getStatus()).thenReturn("requires_confirmation");
        when(paymentIntentService.create(any(PaymentIntentCreateParams.class), any()))
                .thenReturn(mockIntent);

        // 4. Initiate payment with valid JWT token
        mockMvc.perform(post("/v1/payments")
                .with(jwt().jwt(jwt -> jwt
                        .subject(buyerId.toString())
                        .claim("role", "BUYER"))
                        .authorities(new SimpleGrantedAuthority("ROLE_BUYER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "orderId", orderId,
                        "buyerId", buyerId,
                        "method", "CARD",
                        "paymentDetails", Map.of("gatewayToken", "tok_visa")
                ))))
                .andExpect(status().isCreated());

        // Assert payment is created in the database with status INITIATED
        Optional<Payment> optPayment = paymentRepository.findByOrderId(orderId);
        assertThat(optPayment).isPresent();
        Payment payment = optPayment.get();
        assertThat(payment.getStatus()).isEqualTo(Payment.PaymentStatus.INITIATED);
        assertThat(payment.getGatewayReference()).isEqualTo("pi_test_123");

        // 5. Mock Stripe event and webhook signature verification
        Event mockEvent = mock(Event.class);
        when(mockEvent.getType()).thenReturn("payment_intent.succeeded");

        PaymentIntent paymentIntent = mock(PaymentIntent.class);
        when(paymentIntent.getId()).thenReturn("pi_test_123");
        when(paymentIntent.getMetadata()).thenReturn(Map.of(
                "paymentId", payment.getPaymentId().toString(),
                "orderId", orderId.toString()
        ));

        EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);
        when(mockEvent.getDataObjectDeserializer()).thenReturn(deserializer);
        when(deserializer.getObject()).thenReturn(Optional.of(paymentIntent));

        when(signatureVerifier.verifyAndConstruct(anyString(), anyString())).thenReturn(mockEvent);

        // Declare a test queue and bind it to verify RabbitMQ event publication
        String testQueueName = "test.payment.authorized.queue-" + UUID.randomUUID();
        Queue testQueue = new Queue(testQueueName, false, false, true);
        amqpAdmin.declareQueue(testQueue);
        Binding binding = BindingBuilder.bind(testQueue)
                .to(new TopicExchange("payments.events"))
                .with("payment.authorized");
        amqpAdmin.declareBinding(binding);

        try {
            // Trigger Stripe webhook call
            mockMvc.perform(post("/v1/payments/stripe/webhook")
                    .header("Stripe-Signature", "t=123,v1=abc")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"mock\":\"payload\"}"))
                    .andExpect(status().isOk());

            // Verify payment status in database is updated to AUTHORIZED
            Optional<Payment> updatedPaymentOpt = paymentRepository.findById(payment.getPaymentId());
            assertThat(updatedPaymentOpt).isPresent();
            assertThat(updatedPaymentOpt.get().getStatus()).isEqualTo(Payment.PaymentStatus.AUTHORIZED);

            // Verify message published to RabbitMQ
            Object messageObj = rabbitTemplate.receiveAndConvert(testQueueName, 5000);
            assertThat(messageObj).isNotNull();

            @SuppressWarnings("unchecked")
            Map<String, Object> receivedMessage = (Map<String, Object>) messageObj;
            assertThat(receivedMessage.get("eventType")).isEqualTo("PaymentAuthorized");

            @SuppressWarnings("unchecked")
            Map<String, Object> receivedPayload = (Map<String, Object>) receivedMessage.get("payload");
            assertThat(receivedPayload.get("paymentId")).isEqualTo(payment.getPaymentId().toString());
            assertThat(receivedPayload.get("orderId")).isEqualTo(orderId.toString());
            assertThat(receivedPayload.get("status")).isEqualTo("AUTHORIZED");

        } finally {
            amqpAdmin.deleteQueue(testQueueName);
        }
    }
}
