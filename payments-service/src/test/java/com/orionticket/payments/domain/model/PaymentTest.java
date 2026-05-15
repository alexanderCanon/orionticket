package com.orionticket.payments.domain.model;

import com.orionticket.payments.domain.exception.InvalidPaymentStateException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentTest {

    @Test
    void initiateCreatesPaymentInInitiatedState() {
        UUID orderId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();

        Payment payment = Payment.initiate(
                orderId,
                buyerId,
                new BigDecimal("125.00"),
                new BigDecimal("10.00"),
                "GTQ",
                Payment.PaymentMethod.CARD,
                "pay-" + orderId);

        assertThat(payment.getPaymentId()).isNotNull();
        assertThat(payment.getOrderId()).isEqualTo(orderId);
        assertThat(payment.getBuyerId()).isEqualTo(buyerId);
        assertThat(payment.getAmount()).isEqualByComparingTo("125.00");
        assertThat(payment.getServiceFee()).isEqualByComparingTo("10.00");
        assertThat(payment.getCurrency()).isEqualTo("GTQ");
        assertThat(payment.getMethod()).isEqualTo(Payment.PaymentMethod.CARD);
        assertThat(payment.getStatus()).isEqualTo(Payment.PaymentStatus.INITIATED);
        assertThat(payment.getGatewayReference()).isNull();
        assertThat(payment.getCreatedAt()).isNotNull();
    }

    @Test
    void initiateRejectsServiceFeeGreaterThanAmount() {
        assertThatThrownBy(() -> Payment.initiate(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                new BigDecimal("100.01"),
                "GTQ",
                Payment.PaymentMethod.CARD,
                "pay-key"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("serviceFee cannot exceed amount");
    }

    @Test
    void authorizeMovesInitiatedPaymentToAuthorized() {
        Payment payment = validPayment();

        payment.authorize("gw-123");

        assertThat(payment.getStatus()).isEqualTo(Payment.PaymentStatus.AUTHORIZED);
        assertThat(payment.getGatewayReference()).isEqualTo("gw-123");
    }

    @Test
    void authorizeRejectsPaymentThatIsAlreadyFailed() {
        Payment payment = validPayment();
        payment.fail();

        assertThatThrownBy(() -> payment.authorize("gw-123"))
                .isInstanceOf(InvalidPaymentStateException.class)
                .hasMessageContaining("INITIATED");
    }

    @Test
    void failMovesInitiatedPaymentToFailed() {
        Payment payment = validPayment();

        payment.fail();

        assertThat(payment.getStatus()).isEqualTo(Payment.PaymentStatus.FAILED);
    }

    private static Payment validPayment() {
        return Payment.initiate(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("125.00"),
                new BigDecimal("10.00"),
                "GTQ",
                Payment.PaymentMethod.CARD,
                "pay-" + UUID.randomUUID());
    }
}
