package com.orionticket.payments.infrastructure.adapters.in.messaging;

import com.orionticket.payments.infrastructure.adapters.out.persistence.entity.OrderProjectionEntity;
import com.orionticket.payments.infrastructure.adapters.out.persistence.repository.SpringDataOrderProjectionRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderCreatedConsumerTest {

    private final SpringDataOrderProjectionRepository repository = mock(SpringDataOrderProjectionRepository.class);
    private final OrderCreatedConsumer consumer = new OrderCreatedConsumer(repository);

    @Test
    void consumeCreatesOrderProjectionFromOrderCreatedEvent() {
        UUID orderId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID dateId = UUID.randomUUID();
        when(repository.findById(orderId)).thenReturn(Optional.empty());

        consumer.consume(message(orderId, buyerId, eventId, dateId, "CREATED"));

        verify(repository).save(any(OrderProjectionEntity.class));
    }

    @Test
    void consumeUpdatesExistingProjectionStatusIdempotently() {
        UUID orderId = UUID.randomUUID();
        OrderProjectionEntity existing = new OrderProjectionEntity();
        existing.setOrderId(orderId);
        existing.setBuyerId(UUID.randomUUID());
        existing.setEventId(UUID.randomUUID());
        existing.setDateId(UUID.randomUUID());
        existing.setTotal(new BigDecimal("10.00"));
        existing.setServiceFee(new BigDecimal("1.00"));
        existing.setCurrency("GTQ");
        existing.setStatus("CREATED");
        existing.setReceivedAt(Instant.now());
        when(repository.findById(orderId)).thenReturn(Optional.of(existing));

        consumer.consume(message(orderId, existing.getBuyerId(), existing.getEventId(), existing.getDateId(), "CONFIRMED"));

        assertThat(existing.getStatus()).isEqualTo("CONFIRMED");
        verify(repository).save(existing);
    }

    @Test
    void consumeIgnoresNullPayload() {
        consumer.consume(new OrderCreatedConsumer.OrderCreatedMessage("OrderCreated", UUID.randomUUID().toString(), Instant.now().toString(), null));

        verify(repository, never()).save(any());
    }

    private static OrderCreatedConsumer.OrderCreatedMessage message(
            UUID orderId, UUID buyerId, UUID eventId, UUID dateId, String status) {
        return new OrderCreatedConsumer.OrderCreatedMessage(
                "OrderCreated",
                UUID.randomUUID().toString(),
                Instant.now().toString(),
                new OrderCreatedConsumer.OrderCreatedMessage.Payload(
                        orderId,
                        buyerId,
                        eventId,
                        dateId,
                        new BigDecimal("250.00"),
                        new BigDecimal("20.00"),
                        "GTQ",
                        status));
    }
}
