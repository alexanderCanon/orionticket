package com.orionticket.orders.order.application.service;

import com.orionticket.orders.order.application.port.in.CreateOrderCommand;
import com.orionticket.orders.order.domain.exception.OrderAlreadyExistsException;
import com.orionticket.orders.order.domain.exception.OrderNotFoundException;
import com.orionticket.orders.order.domain.exception.ReservationSnapshotNotFoundException;
import com.orionticket.orders.order.domain.model.*;
import com.orionticket.orders.order.domain.port.out.DomainEventPublisherPort;
import com.orionticket.orders.order.domain.port.out.OrderRepositoryPort;
import com.orionticket.orders.order.domain.port.out.ReservationSnapshotRepositoryPort;
import com.orionticket.orders.promotion.domain.exception.InvalidPromotionCodeException;
import com.orionticket.orders.promotion.domain.exception.PromotionExhaustedException;
import com.orionticket.orders.promotion.domain.model.DiscountType;
import com.orionticket.orders.promotion.domain.model.Promotion;
import com.orionticket.orders.promotion.domain.model.PromotionStatus;
import com.orionticket.orders.promotion.domain.port.out.PromotionRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepositoryPort orderRepository;
    @Mock private ReservationSnapshotRepositoryPort snapshotRepository;
    @Mock private PromotionRepositoryPort promotionRepository;
    @Mock private DomainEventPublisherPort eventPublisher;

    @InjectMocks
    private OrderService orderService;

    private UUID buyerId;
    private UUID eventId;
    private UUID dateId;
    private UUID reservationId;
    private ReservationSnapshot validSnapshot;

    @BeforeEach
    void setUp() {
        // Inyectar el rate de service fee manualmente (Spring no lo hace en tests unitarios)
        ReflectionTestUtils.setField(orderService, "serviceFeeRate", new BigDecimal("0.10"));

        buyerId       = UUID.randomUUID();
        eventId       = UUID.randomUUID();
        dateId        = UUID.randomUUID();
        reservationId = UUID.randomUUID();

        validSnapshot = ReservationSnapshot.builder()
                .reservationId(reservationId)
                .seatId(UUID.randomUUID())
                .batchId(UUID.randomUUID())
                .batchPrice(new BigDecimal("100.00"))
                .buyerId(buyerId)
                .eventId(eventId)
                .dateId(dateId)
                .expiresAt(Instant.now().plusSeconds(600))  // vence en 10 min
                .receivedAt(Instant.now())
                .build();
    }

    @Test
    void createOrder_whenValidRequest_shouldPersistOrderAndPublishEvent() {
        CreateOrderCommand command = buildCommand(null);
        when(snapshotRepository.findById(reservationId)).thenReturn(Optional.of(validSnapshot));
        when(orderRepository.findByReservationId(reservationId)).thenReturn(Optional.empty());
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Order order = orderService.createOrder(command);

        assertThat(order).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getSubtotal()).isEqualByComparingTo("100.00");
        assertThat(order.getServiceFee()).isEqualByComparingTo("10.00");   // 10% de 100
        assertThat(order.getTotal()).isEqualByComparingTo("110.00");
        assertThat(order.getPromotionDiscount()).isEqualByComparingTo("0");
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrder_whenPromotionCodeApplied_shouldCalculateDiscountAndFee() {
        String code = "PROMO20";
        Promotion promo = buildActivePromotion(code, DiscountType.PERCENTAGE, new BigDecimal("20"), 10);

        CreateOrderCommand command = buildCommand(code);
        when(snapshotRepository.findById(reservationId)).thenReturn(Optional.of(validSnapshot));
        when(orderRepository.findByReservationId(reservationId)).thenReturn(Optional.empty());
        when(promotionRepository.findByCodeAndEventId(code, eventId)).thenReturn(Optional.of(promo));
        when(promotionRepository.findByIdWithLock(promo.getPromotionId())).thenReturn(Optional.of(promo));
        when(promotionRepository.save(any())).thenReturn(promo);
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Order order = orderService.createOrder(command);

        // subtotal=100, descuento=20%, discount=20, discounted=80, fee=8, total=88
        assertThat(order.getPromotionDiscount()).isEqualByComparingTo("20.00");
        assertThat(order.getServiceFee()).isEqualByComparingTo("8.00");
        assertThat(order.getTotal()).isEqualByComparingTo("88.00");
    }

    @Test
    void createOrder_whenPromotionFixed_shouldNotExceedSubtotal() {
        // Descuento fijo de 200 sobre subtotal de 100 — el descuento no puede superar el subtotal
        String code = "FIXED200";
        Promotion promo = buildActivePromotion(code, DiscountType.FIXED, new BigDecimal("200"), 5);

        CreateOrderCommand command = buildCommand(code);
        when(snapshotRepository.findById(reservationId)).thenReturn(Optional.of(validSnapshot));
        when(orderRepository.findByReservationId(reservationId)).thenReturn(Optional.empty());
        when(promotionRepository.findByCodeAndEventId(code, eventId)).thenReturn(Optional.of(promo));
        when(promotionRepository.findByIdWithLock(promo.getPromotionId())).thenReturn(Optional.of(promo));
        when(promotionRepository.save(any())).thenReturn(promo);
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Order order = orderService.createOrder(command);

        assertThat(order.getPromotionDiscount()).isEqualByComparingTo("100.00"); // capped al subtotal
        assertThat(order.getServiceFee()).isEqualByComparingTo("0.00");          // 10% de 0 = 0
        assertThat(order.getTotal()).isEqualByComparingTo("0.00");
    }

    @Test
    void createOrder_whenPromotionExhausted_shouldThrow422() {
        String code = "AGOTADO";
        Promotion exhausted = buildActivePromotion(code, DiscountType.PERCENTAGE, new BigDecimal("10"), 1);
        exhausted.setUsedCount(1);                          // ya agotada
        exhausted.setStatus(PromotionStatus.EXHAUSTED);

        CreateOrderCommand command = buildCommand(code);
        when(snapshotRepository.findById(reservationId)).thenReturn(Optional.of(validSnapshot));
        when(orderRepository.findByReservationId(reservationId)).thenReturn(Optional.empty());
        when(promotionRepository.findByCodeAndEventId(code, eventId)).thenReturn(Optional.of(exhausted));

        assertThatThrownBy(() -> orderService.createOrder(command))
                .isInstanceOf(PromotionExhaustedException.class);
    }

    @Test
    void createOrder_whenOrderAlreadyExists_shouldThrow409() {
        CreateOrderCommand command = buildCommand(null);
        Order existing = Order.create(buyerId, eventId, dateId, reservationId,
                new ArrayList<>(), BigDecimal.TEN, null, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.valueOf(11), "GTQ");

        when(snapshotRepository.findById(reservationId)).thenReturn(Optional.of(validSnapshot));
        when(orderRepository.findByReservationId(reservationId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> orderService.createOrder(command))
                .isInstanceOf(OrderAlreadyExistsException.class);
    }

    @Test
    void createOrder_whenReservationSnapshotNotFound_shouldThrow404() {
        CreateOrderCommand command = buildCommand(null);
        when(snapshotRepository.findById(reservationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(command))
                .isInstanceOf(ReservationSnapshotNotFoundException.class);
    }

    @Test
    void createOrder_whenReservationExpired_shouldThrow404() {
        ReservationSnapshot expiredSnapshot = ReservationSnapshot.builder()
                .reservationId(reservationId)
                .seatId(UUID.randomUUID()).batchId(UUID.randomUUID())
                .batchPrice(new BigDecimal("100.00"))
                .buyerId(buyerId).eventId(eventId).dateId(dateId)
                .expiresAt(Instant.now().minusSeconds(60))  // ya venció
                .receivedAt(Instant.now())
                .build();

        CreateOrderCommand command = buildCommand(null);
        when(snapshotRepository.findById(reservationId)).thenReturn(Optional.of(expiredSnapshot));

        assertThatThrownBy(() -> orderService.createOrder(command))
                .isInstanceOf(ReservationSnapshotNotFoundException.class);
    }

    @Test
    void expireOrder_whenReservationExpiredReceived_shouldTransitionToExpired() {
        Order activeOrder = Order.create(buyerId, eventId, dateId, reservationId,
                new ArrayList<>(), BigDecimal.TEN, null, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.valueOf(11), "GTQ");

        when(orderRepository.findByReservationId(reservationId)).thenReturn(Optional.of(activeOrder));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        orderService.expireOrderByReservation(reservationId);

        assertThat(activeOrder.getStatus()).isEqualTo(OrderStatus.EXPIRED);
        verify(orderRepository).save(activeOrder);
    }

    @Test
    void expireOrder_whenOrderNotFound_shouldLogAndSkip() {
        // Si no hay orden para la reserva, no debe lanzar excepción — solo log warn
        when(orderRepository.findByReservationId(reservationId)).thenReturn(Optional.empty());

        assertThatCode(() -> orderService.expireOrderByReservation(reservationId))
                .doesNotThrowAnyException();

        verify(orderRepository, never()).save(any());
    }

    @Test
    void confirmOrder_whenPaymentAuthorized_shouldTransitionToConfirmed() {
        UUID orderId   = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        Order order = Order.create(buyerId, eventId, dateId, reservationId,
                new ArrayList<>(), BigDecimal.TEN, null, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.valueOf(11), "GTQ");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        orderService.confirmOrder(orderId, paymentId);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        verify(orderRepository).save(order);
    }

    @Test
    void getOrderById_whenNotFound_shouldThrow404() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(orderId))
                .isInstanceOf(OrderNotFoundException.class);
    }

    // Helpers

    private CreateOrderCommand buildCommand(String promotionCode) {
        return CreateOrderCommand.builder()
                .buyerId(buyerId).eventId(eventId).dateId(dateId)
                .reservationId(reservationId).promotionCode(promotionCode)
                .build();
    }

    private Promotion buildActivePromotion(String code, DiscountType type, BigDecimal value, int maxUses) {
        return Promotion.builder()
                .promotionId(UUID.randomUUID())
                .eventId(eventId)
                .code(code)
                .discountType(type)
                .discountValue(value)
                .maxUses(maxUses)
                .usedCount(0)
                .status(PromotionStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
