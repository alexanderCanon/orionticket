package com.orionticket.seating.batch.application.service;

import com.orionticket.seating.batch.domain.model.Batch;
import com.orionticket.seating.batch.domain.port.out.BatchRepositoryPort;
import com.orionticket.seating.reservation.domain.port.out.DomainEventPublisherPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchManagementServiceTest {

    @Mock
    private BatchRepositoryPort batchRepository;

    @Mock
    private DomainEventPublisherPort eventPublisher;

    @InjectMocks
    private BatchManagementService batchManagementService;

    private UUID eventId;
    private UUID dateId;

    @BeforeEach
    void setUp() {
        eventId = UUID.randomUUID();
        dateId  = UUID.randomUUID();
    }

    @Test
    void createBatch_withFutureDate_shouldBeScheduled() {
        ZonedDateTime futureDate = ZonedDateTime.now().plusDays(1);

        when(batchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Batch result = batchManagementService.createBatch(
                eventId, dateId, "VIP", BigDecimal.valueOf(500), "GTQ", 100, futureDate);

        assertThat(result.getStatus()).isEqualTo("SCHEDULED");
        assertThat(result.getSold()).isZero();
        verify(eventPublisher).publishBatchCreated(result);
    }

    @Test
    void createBatch_withPastDate_shouldBeActive() {
        ZonedDateTime pastDate = ZonedDateTime.now().minusDays(1);

        when(batchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Batch result = batchManagementService.createBatch(
                eventId, dateId, "General", BigDecimal.valueOf(100), "GTQ", 50, pastDate);

        assertThat(result.getStatus()).isEqualTo("ACTIVE");
        verify(eventPublisher).publishBatchCreated(result);
    }

    @Test
    void createBatch_shouldPublishBatchCreatedEvent() {
        when(batchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        batchManagementService.createBatch(
                eventId, dateId, "Test", BigDecimal.ONE, "GTQ", 10, ZonedDateTime.now().plusHours(1));

        verify(eventPublisher, times(1)).publishBatchCreated(any());
    }
}
