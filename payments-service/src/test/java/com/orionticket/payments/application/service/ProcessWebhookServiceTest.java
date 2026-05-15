package com.orionticket.payments.application.service;

import com.orionticket.payments.application.port.out.PaymentEventPublisherPort;
import com.orionticket.payments.domain.exception.PaymentNotFoundException;
import com.orionticket.payments.domain.model.Payment;
import com.orionticket.payments.domain.port.out.PaymentRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class ProcessWebhookServiceTest {

    @Mock
    private PaymentRepositoryPort paymentRepository;

    @Mock
    private PaymentEventPublisherPort eventPublisher;

    @InjectMocks
    private ProcessWebhookService service;

    @Test
    void processWebhookAuthorizesInitiatedPaymentAndPublishesEvent() {
        Payment payment = initiatedPayment();
        when(paymentRepository.findById(payment.getPaymentId())).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.processWebhook(payment.getPaymentId(), "gw-123", "AUTHORIZED", null);

        assertThat(payment.getStatus()).isEqualTo(Payment.PaymentStatus.AUTHORIZED);
        assertThat(payment.getGatewayReference()).isEqualTo("gw-123");
        verify(eventPublisher).publishPaymentAuthorized(any(PaymentEventPublisherPort.PaymentEvent.class));
        verify(eventPublisher, never()).publishPaymentFailed(any());
    }

    @Test
    void processWebhookFailsInitiatedPaymentAndPublishesFailureEvent() {
        Payment payment = initiatedPayment();
        when(paymentRepository.findById(payment.getPaymentId())).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.processWebhook(payment.getPaymentId(), "gw-123", "FAILED", "insufficient funds");

        assertThat(payment.getStatus()).isEqualTo(Payment.PaymentStatus.FAILED);
        verify(eventPublisher).publishPaymentFailed(any(PaymentEventPublisherPort.PaymentFailedEvent.class));
        verify(eventPublisher, never()).publishPaymentAuthorized(any());
    }

    @Test
    void processWebhookIgnoresDuplicateTerminalWebhook() {
        Payment payment = initiatedPayment();
        payment.authorize("gw-123");
        when(paymentRepository.findById(payment.getPaymentId())).thenReturn(Optional.of(payment));

        service.processWebhook(payment.getPaymentId(), "gw-123", "AUTHORIZED", null);

        verify(paymentRepository, never()).save(any());
        verify(eventPublisher, never()).publishPaymentAuthorized(any());
        verify(eventPublisher, never()).publishPaymentFailed(any());
    }

    @Test
    void processWebhookRejectsUnknownResult() {
        Payment payment = initiatedPayment();
        when(paymentRepository.findById(payment.getPaymentId())).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> service.processWebhook(payment.getPaymentId(), "gw-123", "PENDING", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown webhook result");
    }

    @Test
    void processWebhookThrowsWhenPaymentDoesNotExist() {
        UUID paymentId = UUID.randomUUID();
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.processWebhook(paymentId, "gw-123", "AUTHORIZED", null))
                .isInstanceOf(PaymentNotFoundException.class);
    }

    private static Payment initiatedPayment() {
        return Payment.initiate(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("250.00"),
                new BigDecimal("20.00"),
                "GTQ",
                Payment.PaymentMethod.CARD,
                "pay-" + UUID.randomUUID());
    }
}
