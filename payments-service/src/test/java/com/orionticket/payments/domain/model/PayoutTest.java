package com.orionticket.payments.domain.model;

import com.orionticket.payments.domain.exception.InvalidPaymentStateException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PayoutTest {

    @Test
    void generateCalculatesNetAmountAndStartsPending() {
        Payout payout = Payout.generate(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("1000.00"),
                new BigDecimal("85.50"));

        assertThat(payout.getPayoutId()).isNotNull();
        assertThat(payout.getGrossAmount()).isEqualByComparingTo("1000.00");
        assertThat(payout.getServiceFeeTotal()).isEqualByComparingTo("85.50");
        assertThat(payout.getNetAmount()).isEqualByComparingTo("914.50");
        assertThat(payout.getStatus()).isEqualTo(Payout.PayoutStatus.PENDING);
        assertThat(payout.getRetryCount()).isZero();
        assertThat(payout.getTriggeredAt()).isNotNull();
        assertThat(payout.getProcessedAt()).isNull();
    }

    @Test
    void generateRejectsServiceFeeGreaterThanGrossAmount() {
        assertThatThrownBy(() -> Payout.generate(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                new BigDecimal("100.01")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("serviceFeeTotal cannot exceed grossAmount");
    }

    @Test
    void markProcessedMovesPendingPayoutToProcessed() {
        Payout payout = validPayout();
        Instant processedAt = Instant.parse("2026-05-15T12:00:00Z");

        payout.markProcessed(processedAt);

        assertThat(payout.getStatus()).isEqualTo(Payout.PayoutStatus.PROCESSED);
        assertThat(payout.getProcessedAt()).isEqualTo(processedAt);
    }

    @Test
    void retryAllowsOneAutomaticRetryForFailedPayout() {
        Payout payout = validPayout();
        payout.markFailed();

        payout.retry();

        assertThat(payout.getStatus()).isEqualTo(Payout.PayoutStatus.PENDING);
        assertThat(payout.getRetryCount()).isEqualTo(1);
    }

    @Test
    void retryRejectsPayoutAfterRetryLimitIsReached() {
        Payout payout = validPayout();
        payout.markFailed();
        payout.retry();
        payout.markFailed();

        assertThatThrownBy(payout::retry)
                .isInstanceOf(InvalidPaymentStateException.class)
                .hasMessageContaining("exhausted");
    }

    private static Payout validPayout() {
        return Payout.generate(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("1000.00"),
                new BigDecimal("85.50"));
    }
}
