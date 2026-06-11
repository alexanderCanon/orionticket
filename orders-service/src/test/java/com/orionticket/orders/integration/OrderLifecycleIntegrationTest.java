package com.orionticket.orders.integration;

import com.orionticket.orders.order.application.port.in.CreateOrderCommand;
import com.orionticket.orders.order.application.port.in.OrderUseCase;
import com.orionticket.orders.order.domain.model.Order;
import com.orionticket.orders.order.domain.model.OrderStatus;
import com.orionticket.orders.order.domain.model.ReservationSnapshot;
import com.orionticket.orders.order.domain.port.out.DomainEventPublisherPort;
import com.orionticket.orders.order.infrastructure.adapters.in.messaging.PaymentAuthorizedConsumer;
import com.orionticket.orders.order.infrastructure.adapters.in.messaging.ReservationExpiredConsumer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@Testcontainers
class OrderLifecycleIntegrationTest {

    @Container
    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("orders_db")
            .withUsername("orion")
            .withPassword("secret");

    @DynamicPropertySource
    static void configProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.rabbitmq.host", () -> "localhost");
        registry.add("spring.rabbitmq.port", () -> "5672");
    }

    @MockBean
    private DomainEventPublisherPort eventPublisher;

    @Autowired private OrderUseCase orderUseCase;
    @Autowired private ReservationExpiredConsumer reservationExpiredConsumer;
    @Autowired private PaymentAuthorizedConsumer paymentAuthorizedConsumer;

    @Test
    void expireOrder_whenReservationExpiredReceived_shouldTransitionToExpired() {
        // Crear una orden en DB
        UUID reservationId = UUID.randomUUID();
        Order createdOrder = createOrderInDb(reservationId);

        // Simular el evento ReservationExpired directamente (como lo haría RabbitMQ)
        Map<String, Object> event = buildEvent("ReservationExpired", Map.of(
                "reservationId", reservationId.toString(),
                "buyerId", createdOrder.getBuyerId().toString(),
                "status", "EXPIRED"
        ));
        reservationExpiredConsumer.handleEvent(event);

        // Verificar que la orden quedó EXPIRED en DB
        Order expired = orderUseCase.getOrderById(createdOrder.getOrderId());
        assertThat(expired.getStatus()).isEqualTo(OrderStatus.EXPIRED);

        // Verificar que el evento OrderExpired fue publicado
        verify(eventPublisher, timeout(1000)).publishOrderExpired(any(Order.class));
    }

    @Test
    void confirmOrder_whenPaymentAuthorizedReceived_shouldTransitionToConfirmed() {
        UUID reservationId = UUID.randomUUID();
        Order createdOrder = createOrderInDb(reservationId);

        UUID paymentId = UUID.randomUUID();
        Map<String, Object> event = buildEvent("PaymentAuthorized", Map.of(
                "orderId", createdOrder.getOrderId().toString(),
                "paymentId", paymentId.toString(),
                "buyerId", createdOrder.getBuyerId().toString(),
                "amount", "110.00",
                "status", "AUTHORIZED"
        ));
        paymentAuthorizedConsumer.handleEvent(event);

        // Verificar que la orden quedó CONFIRMED en DB
        Order confirmed = orderUseCase.getOrderById(createdOrder.getOrderId());
        assertThat(confirmed.getStatus()).isEqualTo(OrderStatus.CONFIRMED);

        // Verificar que el evento OrderConfirmed fue publicado con el paymentId correcto
        verify(eventPublisher, timeout(1000)).publishOrderConfirmed(any(Order.class), eq(paymentId));
    }

    @Test
    void expireOrder_whenOrderNotFound_shouldNotThrow() {
        UUID unknownReservationId = UUID.randomUUID();
        Map<String, Object> event = buildEvent("ReservationExpired", Map.of(
                "reservationId", unknownReservationId.toString(),
                "buyerId", UUID.randomUUID().toString(),
                "status", "EXPIRED"
        ));

        // Si no hay orden para la reserva, el consumer no debe lanzar excepción
        assertThatCode(() -> reservationExpiredConsumer.handleEvent(event))
                .doesNotThrowAnyException();

        verify(eventPublisher, never()).publishOrderExpired(any());
    }

    @Test
    void confirmOrder_whenOrderNotFound_shouldThrow() {
        UUID unknownOrderId = UUID.randomUUID();
        Map<String, Object> event = buildEvent("PaymentAuthorized", Map.of(
                "orderId", unknownOrderId.toString(),
                "paymentId", UUID.randomUUID().toString(),
                "buyerId", UUID.randomUUID().toString(),
                "amount", "100.00",
                "status", "AUTHORIZED"
        ));

        // OrderNotFoundException se convierte en RuntimeException por el consumer
        assertThatThrownBy(() -> paymentAuthorizedConsumer.handleEvent(event))
                .isInstanceOf(RuntimeException.class);
    }

    // Helper: crea una orden real en la DB para usar en los tests de ciclo de vida
    private Order createOrderInDb(UUID reservationId) {
        UUID buyerId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID dateId  = UUID.randomUUID();

        ReservationSnapshot snapshot = ReservationSnapshot.builder()
                .reservationId(reservationId)
                .seatId(UUID.randomUUID())
                .batchId(UUID.randomUUID())
                .batchPrice(new BigDecimal("100.00"))
                .buyerId(buyerId)
                .eventId(eventId)
                .dateId(dateId)
                .expiresAt(Instant.now().plusSeconds(600))
                .receivedAt(Instant.now())
                .build();
        orderUseCase.storeReservationSnapshot(snapshot);

        CreateOrderCommand command = CreateOrderCommand.builder()
                .buyerId(buyerId).eventId(eventId).dateId(dateId)
                .reservationId(reservationId).build();
        return orderUseCase.createOrder(command);
    }

    private Map<String, Object> buildEvent(String eventType, Map<String, Object> payload) {
        return Map.of(
                "eventType", eventType,
                "eventId", UUID.randomUUID().toString(),
                "occurredAt", Instant.now().toString(),
                "payload", payload
        );
    }
}
