package com.orionticket.payments.infrastructure.adapters.in.messaging;

import com.orionticket.payments.infrastructure.adapters.out.persistence.entity.DateProjectionEntity;
import com.orionticket.payments.infrastructure.adapters.out.persistence.repository.SpringDataDateProjectionRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DateAddedConsumerTest {

    private final SpringDataDateProjectionRepository repository = mock(SpringDataDateProjectionRepository.class);
    private final DateAddedConsumer consumer = new DateAddedConsumer(repository);

    @Test
    void consumeCreatesDateProjectionWhenDateIsNew() {
        UUID dateId = UUID.randomUUID();
        when(repository.existsById(dateId)).thenReturn(false);

        consumer.consume(message(dateId, UUID.randomUUID(), Instant.parse("2026-06-01T02:00:00Z")));

        verify(repository).save(any(DateProjectionEntity.class));
    }

    @Test
    void consumeSkipsExistingDateProjectionToPreservePayoutGeneratedFlag() {
        UUID dateId = UUID.randomUUID();
        when(repository.existsById(dateId)).thenReturn(true);

        consumer.consume(message(dateId, UUID.randomUUID(), Instant.parse("2026-06-01T02:00:00Z")));

        verify(repository, never()).save(any());
    }

    @Test
    void consumeIgnoresNullPayload() {
        consumer.consume(new DateAddedConsumer.DateAddedMessage("DateAdded", UUID.randomUUID().toString(), Instant.now().toString(), null));

        verify(repository, never()).save(any());
    }

    private static DateAddedConsumer.DateAddedMessage message(UUID dateId, UUID eventId, Instant scheduledAt) {
        return new DateAddedConsumer.DateAddedMessage(
                "DateAdded",
                UUID.randomUUID().toString(),
                Instant.now().toString(),
                new DateAddedConsumer.DateAddedMessage.Payload(
                        dateId,
                        eventId,
                        scheduledAt.toString(),
                        UUID.randomUUID(),
                        1000));
    }
}
