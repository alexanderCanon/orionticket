package com.orionticket.payments.infrastructure.adapters.out.persistence;

import com.orionticket.payments.domain.model.Payment;
import com.orionticket.payments.infrastructure.adapters.out.persistence.mapper.PaymentMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PaymentRepositoryAdapter.class, PaymentMapper.class})
class PaymentRepositoryAdapterIntegrationTest {

    @Container
    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("payments_test")
            .withUsername("payments")
            .withPassword("payments");

    @DynamicPropertySource
    static void configurePostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
    }

    @Autowired
    private PaymentRepositoryAdapter repository;

    @Test
    void savePersistsPaymentAndFindsItByOrderIdAndIdempotencyKey() {
        UUID orderId = UUID.randomUUID();
        Payment payment = Payment.initiate(
                orderId,
                UUID.randomUUID(),
                new BigDecimal("250.00"),
                new BigDecimal("20.00"),
                "GTQ",
                Payment.PaymentMethod.CARD,
                "pay-" + orderId);

        Payment saved = repository.save(payment);

        Optional<Payment> byOrderId = repository.findByOrderId(orderId);
        Optional<Payment> byIdempotencyKey = repository.findByIdempotencyKey("pay-" + orderId);

        assertThat(saved.getPaymentId()).isEqualTo(payment.getPaymentId());
        assertThat(byOrderId).isPresent();
        assertThat(byOrderId.get().getAmount()).isEqualByComparingTo("250.00");
        assertThat(byIdempotencyKey).isPresent();
        assertThat(byIdempotencyKey.get().getOrderId()).isEqualTo(orderId);
    }

    @Test
    void savePersistsAuthorizedPaymentGatewayReference() {
        Payment payment = Payment.initiate(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("250.00"),
                new BigDecimal("20.00"),
                "GTQ",
                Payment.PaymentMethod.CARD,
                "pay-" + UUID.randomUUID());
        payment.authorize("gw-123");

        repository.save(payment);

        Optional<Payment> found = repository.findById(payment.getPaymentId());
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(Payment.PaymentStatus.AUTHORIZED);
        assertThat(found.get().getGatewayReference()).isEqualTo("gw-123");
    }
}
