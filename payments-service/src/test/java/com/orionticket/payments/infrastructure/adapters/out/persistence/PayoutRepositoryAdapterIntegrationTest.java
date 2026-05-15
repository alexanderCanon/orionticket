package com.orionticket.payments.infrastructure.adapters.out.persistence;

import com.orionticket.payments.domain.model.Payout;
import com.orionticket.payments.infrastructure.adapters.out.persistence.mapper.PayoutMapper;
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
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PayoutRepositoryAdapter.class, PayoutMapper.class})
class PayoutRepositoryAdapterIntegrationTest {

    @Container
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
    private PayoutRepositoryAdapter repository;

    @Test
    void savePersistsPayoutAndFindsItByOrganizer() {
        UUID organizerId = UUID.randomUUID();
        Payout payout = payout(organizerId);

        repository.save(payout);

        List<Payout> found = repository.findByOrganizerId(organizerId);
        assertThat(found).hasSize(1);
        assertThat(found.getFirst().getNetAmount()).isEqualByComparingTo("910.00");
    }

    @Test
    void findByOrganizerIdAndStatusFiltersPayouts() {
        UUID organizerId = UUID.randomUUID();
        Payout pending = payout(organizerId);
        Payout processed = payout(organizerId);
        processed.markProcessed(Instant.parse("2026-05-15T12:00:00Z"));
        repository.save(pending);
        repository.save(processed);

        List<Payout> found = repository.findByOrganizerIdAndStatus(organizerId, Payout.PayoutStatus.PROCESSED);

        assertThat(found).hasSize(1);
        assertThat(found.getFirst().getPayoutId()).isEqualTo(processed.getPayoutId());
        assertThat(found.getFirst().getStatus()).isEqualTo(Payout.PayoutStatus.PROCESSED);
    }

    @Test
    void findByIdReturnsEmptyForUnknownPayout() {
        Optional<Payout> found = repository.findById(UUID.randomUUID());

        assertThat(found).isEmpty();
    }

    private static Payout payout(UUID organizerId) {
        return Payout.generate(
                organizerId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("1000.00"),
                new BigDecimal("90.00"));
    }
}
