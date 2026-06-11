package com.orionticket.orders.integration;

import com.orionticket.orders.order.application.port.in.CreateOrderCommand;
import com.orionticket.orders.order.application.port.in.OrderUseCase;
import com.orionticket.orders.order.domain.exception.OrderAlreadyExistsException;
import com.orionticket.orders.order.domain.exception.ReservationSnapshotNotFoundException;
import com.orionticket.orders.order.domain.model.Order;
import com.orionticket.orders.order.domain.model.OrderStatus;
import com.orionticket.orders.order.domain.model.ReservationSnapshot;
import com.orionticket.orders.order.domain.port.out.DomainEventPublisherPort;
import com.orionticket.orders.promotion.domain.exception.PromotionExhaustedException;
import com.orionticket.orders.promotion.domain.model.DiscountType;
import com.orionticket.orders.promotion.domain.model.Promotion;
import com.orionticket.orders.promotion.domain.port.out.PromotionRepositoryPort;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@Testcontainers
class CreateOrderIntegrationTest {

    // PostgreSQL real — Flyway corre las 4 migraciones al arrancar el contexto
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
        // Evitar que Spring AMQP intente conectar a RabbitMQ en tests
        registry.add("spring.rabbitmq.host", () -> "localhost");
        registry.add("spring.rabbitmq.port", () -> "5672");
    }

    // Publisher mockeado — verificamos que se llama, sin necesitar RabbitMQ real
    @MockBean
    private DomainEventPublisherPort eventPublisher;

    @Autowired private OrderUseCase orderUseCase;
    @Autowired private PromotionRepositoryPort promotionRepository;

    @Test
    void createOrder_happyPath_shouldPersistAndPublishEvent() {
        UUID reservationId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID dateId  = UUID.randomUUID();

        // Insertar snapshot en DB (simula haber recibido ReservationCreated)
        ReservationSnapshot snapshot = buildSnapshot(reservationId, buyerId, eventId, dateId,
                new BigDecimal("150.00"), Instant.now().plusSeconds(600));
        orderUseCase.storeReservationSnapshot(snapshot);

        CreateOrderCommand command = CreateOrderCommand.builder()
                .buyerId(buyerId).eventId(eventId).dateId(dateId)
                .reservationId(reservationId).build();

        Order order = orderUseCase.createOrder(command);

        assertThat(order).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getSubtotal()).isEqualByComparingTo("150.00");
        assertThat(order.getServiceFee()).isEqualByComparingTo("15.00");
        assertThat(order.getTotal()).isEqualByComparingTo("165.00");
        assertThat(order.getLineItems()).hasSize(1);
        assertThat(order.getLineItems().get(0).getSeatId()).isNotNull();

        // Verificar que el evento fue publicado después del commit
        verify(eventPublisher, timeout(1000)).publishOrderCreated(any(Order.class));
    }

    @Test
    void createOrder_duplicateReservation_shouldReturn409() {
        UUID reservationId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID dateId  = UUID.randomUUID();

        ReservationSnapshot snapshot = buildSnapshot(reservationId, buyerId, eventId, dateId,
                new BigDecimal("100.00"), Instant.now().plusSeconds(600));
        orderUseCase.storeReservationSnapshot(snapshot);

        CreateOrderCommand command = CreateOrderCommand.builder()
                .buyerId(buyerId).eventId(eventId).dateId(dateId)
                .reservationId(reservationId).build();

        // @MockBean hace void methods no-ops automáticamente; no se necesita stubbing

        // Primera orden — OK
        orderUseCase.createOrder(command);

        // Segunda orden con la misma reserva — debe lanzar 409
        assertThatThrownBy(() -> orderUseCase.createOrder(command))
                .isInstanceOf(OrderAlreadyExistsException.class);
    }

    @Test
    void createOrder_withPercentagePromotion_shouldApplyDiscountCorrectly() {
        UUID eventId  = UUID.randomUUID();
        UUID buyerId  = UUID.randomUUID();
        UUID dateId   = UUID.randomUUID();
        UUID reservId = UUID.randomUUID();

        // Guardar promoción en DB
        Promotion promo = Promotion.create(eventId, "DESCUENTO15", DiscountType.PERCENTAGE,
                new BigDecimal("15"), 10);
        promotionRepository.save(promo);

        ReservationSnapshot snapshot = buildSnapshot(reservId, buyerId, eventId, dateId,
                new BigDecimal("200.00"), Instant.now().plusSeconds(600));
        orderUseCase.storeReservationSnapshot(snapshot);

        CreateOrderCommand command = CreateOrderCommand.builder()
                .buyerId(buyerId).eventId(eventId).dateId(dateId)
                .reservationId(reservId).promotionCode("DESCUENTO15").build();

        Order order = orderUseCase.createOrder(command);

        // subtotal=200, discount=15%=30, discounted=170, fee=17, total=187
        assertThat(order.getPromotionDiscount()).isEqualByComparingTo("30.00");
        assertThat(order.getServiceFee()).isEqualByComparingTo("17.00");
        assertThat(order.getTotal()).isEqualByComparingTo("187.00");
        assertThat(order.getPromotionId()).isEqualTo(promo.getPromotionId());
    }

    @Test
    void createOrder_whenPromotionLastUse_shouldMarkExhaustedAndPreventReuse() {
        UUID eventId  = UUID.randomUUID();
        UUID buyerId1 = UUID.randomUUID();
        UUID buyerId2 = UUID.randomUUID();
        UUID dateId   = UUID.randomUUID();

        // Promoción de un solo uso
        Promotion promo = Promotion.create(eventId, "UNICO", DiscountType.FIXED,
                new BigDecimal("50"), 1);
        promotionRepository.save(promo);

        UUID reservId1 = UUID.randomUUID();
        UUID reservId2 = UUID.randomUUID();

        orderUseCase.storeReservationSnapshot(buildSnapshot(reservId1, buyerId1, eventId, dateId,
                new BigDecimal("100.00"), Instant.now().plusSeconds(600)));
        orderUseCase.storeReservationSnapshot(buildSnapshot(reservId2, buyerId2, eventId, dateId,
                new BigDecimal("100.00"), Instant.now().plusSeconds(600)));

        // Primer uso — OK
        orderUseCase.createOrder(CreateOrderCommand.builder()
                .buyerId(buyerId1).eventId(eventId).dateId(dateId)
                .reservationId(reservId1).promotionCode("UNICO").build());

        // Segundo uso — debe fallar (EXHAUSTED)
        assertThatThrownBy(() -> orderUseCase.createOrder(CreateOrderCommand.builder()
                .buyerId(buyerId2).eventId(eventId).dateId(dateId)
                .reservationId(reservId2).promotionCode("UNICO").build()))
                .isInstanceOf(PromotionExhaustedException.class);
    }

    @Test
    void createOrder_whenReservationExpired_shouldThrow404() {
        UUID reservationId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID dateId  = UUID.randomUUID();

        // Snapshot con reserva ya vencida
        ReservationSnapshot expiredSnapshot = buildSnapshot(reservationId, buyerId, eventId, dateId,
                new BigDecimal("100.00"), Instant.now().minusSeconds(60));
        orderUseCase.storeReservationSnapshot(expiredSnapshot);

        CreateOrderCommand command = CreateOrderCommand.builder()
                .buyerId(buyerId).eventId(eventId).dateId(dateId)
                .reservationId(reservationId).build();

        assertThatThrownBy(() -> orderUseCase.createOrder(command))
                .isInstanceOf(ReservationSnapshotNotFoundException.class);
    }

    private ReservationSnapshot buildSnapshot(UUID reservationId, UUID buyerId,
                                              UUID eventId, UUID dateId,
                                              BigDecimal batchPrice, Instant expiresAt) {
        return ReservationSnapshot.builder()
                .reservationId(reservationId)
                .seatId(UUID.randomUUID())
                .batchId(UUID.randomUUID())
                .batchPrice(batchPrice)
                .buyerId(buyerId)
                .eventId(eventId)
                .dateId(dateId)
                .expiresAt(expiresAt)
                .receivedAt(Instant.now())
                .build();
    }
}
