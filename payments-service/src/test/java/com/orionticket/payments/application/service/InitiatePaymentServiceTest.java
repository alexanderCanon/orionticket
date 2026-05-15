package com.orionticket.payments.application.service;

import com.orionticket.payments.application.port.out.OrderSummaryPort;
import com.orionticket.payments.application.port.out.PaymentEventPublisherPort;
import com.orionticket.payments.application.port.out.PaymentGatewayPort;
import com.orionticket.payments.domain.exception.PaymentGatewayException;
import com.orionticket.payments.domain.model.Payment;
import com.orionticket.payments.domain.port.out.PaymentRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InitiatePaymentServiceTest {

    @Mock
    private PaymentRepositoryPort paymentRepository;

    @Mock
    private OrderSummaryPort orderSummary;

    @Mock
    private PaymentGatewayPort gateway;

    @Mock
    private PaymentEventPublisherPort eventPublisher;

    @InjectMocks
    private InitiatePaymentService service;

    @Test
    void initiateReturnsExistingPaymentWithoutCallingGateway() {
        UUID orderId = UUID.randomUUID();
        Payment existing = payment(orderId, UUID.randomUUID(), Payment.PaymentStatus.INITIATED);
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(existing));

        Payment result = service.initiate(orderId, existing.getBuyerId(), Payment.PaymentMethod.CARD, "tok-card");

        assertThat(result).isSameAs(existing);
        verify(orderSummary, never()).findByOrderId(any());
        verify(gateway, never()).process(any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void initiateCreatesPaymentAndPublishesInitiatedEventWhenGatewayAcceptsSubmission() {
        UUID orderId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(orderSummary.findByOrderId(orderId)).thenReturn(order(orderId, buyerId, "CREATED"));
        when(gateway.process(any())).thenReturn(new PaymentGatewayPort.GatewayResponse(true, "gw-submitted", null));
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Payment result = service.initiate(orderId, buyerId, Payment.PaymentMethod.CARD, "tok-card");

        assertThat(result.getStatus()).isEqualTo(Payment.PaymentStatus.INITIATED);
        assertThat(result.getOrderId()).isEqualTo(orderId);
        assertThat(result.getBuyerId()).isEqualTo(buyerId);
        assertThat(result.getIdempotencyKey()).isEqualTo("pay-" + orderId);

        ArgumentCaptor<PaymentGatewayPort.GatewayRequest> gatewayRequest =
                ArgumentCaptor.forClass(PaymentGatewayPort.GatewayRequest.class);
        verify(gateway).process(gatewayRequest.capture());
        assertThat(gatewayRequest.getValue().idempotencyKey()).isEqualTo("pay-" + orderId);
        assertThat(gatewayRequest.getValue().gatewayToken()).isEqualTo("tok-card");
        assertThat(gatewayRequest.getValue().amount()).isEqualByComparingTo("250.00");

        verify(eventPublisher).publishPaymentInitiated(any(PaymentEventPublisherPort.PaymentEvent.class));
        verify(eventPublisher, never()).publishPaymentFailed(any());
    }

    @Test
    void initiateStoresFailedPaymentAndPublishesFailedEventWhenGatewayRejectsSubmission() {
        UUID orderId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(orderSummary.findByOrderId(orderId)).thenReturn(order(orderId, buyerId, "CREATED"));
        when(gateway.process(any())).thenReturn(new PaymentGatewayPort.GatewayResponse(false, null, "card declined"));
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Payment result = service.initiate(orderId, buyerId, Payment.PaymentMethod.CARD, "tok-card");

        assertThat(result.getStatus()).isEqualTo(Payment.PaymentStatus.FAILED);
        verify(eventPublisher).publishPaymentFailed(any(PaymentEventPublisherPort.PaymentFailedEvent.class));
        verify(eventPublisher, never()).publishPaymentInitiated(any());
    }

    @Test
    void initiateRejectsOrderThatIsNotPayable() {
        UUID orderId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(orderSummary.findByOrderId(orderId)).thenReturn(order(orderId, buyerId, "CONFIRMED"));

        assertThatThrownBy(() -> service.initiate(orderId, buyerId, Payment.PaymentMethod.CARD, "tok-card"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not in a payable state");

        verify(gateway, never()).process(any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void initiateWrapsUnexpectedGatewayFailures() {
        UUID orderId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(orderSummary.findByOrderId(orderId)).thenReturn(order(orderId, buyerId, "CREATED"));
        when(gateway.process(any())).thenThrow(new RuntimeException("timeout"));

        assertThatThrownBy(() -> service.initiate(orderId, buyerId, Payment.PaymentMethod.CARD, "tok-card"))
                .isInstanceOf(PaymentGatewayException.class)
                .hasMessageContaining("unavailable");
    }

    private static OrderSummaryPort.OrderSummary order(UUID orderId, UUID buyerId, String status) {
        return new OrderSummaryPort.OrderSummary(
                orderId,
                buyerId,
                new BigDecimal("250.00"),
                new BigDecimal("20.00"),
                "GTQ",
                status);
    }

    private static Payment payment(UUID orderId, UUID buyerId, Payment.PaymentStatus status) {
        Payment payment = Payment.initiate(
                orderId,
                buyerId,
                new BigDecimal("250.00"),
                new BigDecimal("20.00"),
                "GTQ",
                Payment.PaymentMethod.CARD,
                "pay-" + orderId);
        if (status == Payment.PaymentStatus.AUTHORIZED) {
            payment.authorize("gw-123");
        } else if (status == Payment.PaymentStatus.FAILED) {
            payment.fail();
        }
        return payment;
    }
}
