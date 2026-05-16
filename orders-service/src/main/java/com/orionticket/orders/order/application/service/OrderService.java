package com.orionticket.orders.order.application.service;

import com.orionticket.orders.order.application.port.in.CreateOrderCommand;
import com.orionticket.orders.order.application.port.in.OrderUseCase;
import com.orionticket.orders.order.domain.exception.OrderAlreadyExistsException;
import com.orionticket.orders.order.domain.exception.OrderNotFoundException;
import com.orionticket.orders.order.domain.exception.ReservationSnapshotNotFoundException;
import com.orionticket.orders.order.domain.model.LineItem;
import com.orionticket.orders.order.domain.model.Order;
import com.orionticket.orders.order.domain.model.ReservationSnapshot;
import com.orionticket.orders.order.domain.port.out.DomainEventPublisherPort;
import com.orionticket.orders.order.domain.port.out.OrderRepositoryPort;
import com.orionticket.orders.order.domain.port.out.ReservationSnapshotRepositoryPort;
import com.orionticket.orders.promotion.domain.exception.InvalidPromotionCodeException;
import com.orionticket.orders.promotion.domain.exception.PromotionExhaustedException;
import com.orionticket.orders.promotion.domain.model.Promotion;
import com.orionticket.orders.promotion.domain.port.out.PromotionRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService implements OrderUseCase {

    private final OrderRepositoryPort orderRepository;
    private final ReservationSnapshotRepositoryPort snapshotRepository;
    private final PromotionRepositoryPort promotionRepository;
    private final DomainEventPublisherPort eventPublisher;

    // Porcentaje configurable de service fee (default 10%)
    @Value("${orders.service-fee.rate:0.10}")
    private BigDecimal serviceFeeRate;

    @Transactional
    @Override
    public Order createOrder(CreateOrderCommand command) {

        // 1. Recuperar snapshot de la reserva (poblado por ReservationCreatedConsumer)
        ReservationSnapshot snapshot = snapshotRepository.findById(command.getReservationId())
                .orElseThrow(() -> new ReservationSnapshotNotFoundException(command.getReservationId()));

        // 2. Verificar que la reserva no haya vencido
        if (snapshot.isExpired()) {
            throw new ReservationSnapshotNotFoundException(command.getReservationId());
        }

        // 3. Idempotencia: si ya existe una orden para esta reserva, retornar 409 (ADR-008)
        orderRepository.findByReservationId(command.getReservationId())
                .ifPresent(existing -> {
                    throw new OrderAlreadyExistsException(command.getReservationId());
                });

        // 4. Calcular subtotal desde el snapshot (batchPrice * quantity=1)
        BigDecimal subtotal = snapshot.getBatchPrice();

        // 5. Aplicar promoción si se proveyó un código
        UUID promotionId = null;
        BigDecimal promotionDiscount = BigDecimal.ZERO;
        Promotion appliedPromotion = null;

        if (command.getPromotionCode() != null && !command.getPromotionCode().isBlank()) {
            // Buscar la promoción para este evento
            Promotion promotion = promotionRepository
                    .findByCodeAndEventId(command.getPromotionCode().toUpperCase(), command.getEventId())
                    .orElseThrow(() -> new InvalidPromotionCodeException(command.getPromotionCode()));

            if (!promotion.isAvailable()) {
                throw new PromotionExhaustedException(command.getPromotionCode());
            }

            // Lock pesimista para evitar race condition si dos buyers usan el mismo código simultáneamente
            promotion = promotionRepository.findByIdWithLock(promotion.getPromotionId())
                    .orElseThrow(() -> new InvalidPromotionCodeException(command.getPromotionCode()));

            // Re-verificar disponibilidad después del lock (otro hilo pudo haberla agotado)
            if (!promotion.isAvailable()) {
                throw new PromotionExhaustedException(command.getPromotionCode());
            }

            promotionDiscount = promotion.calculateDiscount(subtotal);
            promotion.incrementUsed();
            promotionRepository.save(promotion);

            promotionId = promotion.getPromotionId();
            appliedPromotion = promotion;
        }

        // 6. Calcular service fee y total
        BigDecimal discountedSubtotal = subtotal.subtract(promotionDiscount);
        BigDecimal serviceFee = discountedSubtotal
                .multiply(serviceFeeRate)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = discountedSubtotal.add(serviceFee);

        // 7. Construir line item (un asiento = un line item)
        UUID tempOrderId = UUID.randomUUID();
        LineItem lineItem = LineItem.create(tempOrderId, snapshot.getSeatId(), snapshot.getBatchPrice());

        // 8. Crear y persistir la orden en la misma transacción
        Order order = Order.create(
                command.getBuyerId(), command.getEventId(), command.getDateId(),
                command.getReservationId(), List.of(lineItem),
                subtotal, promotionId, promotionDiscount, serviceFee, total, "GTQ"
        );
        // Sincronizar el orderId del line item con el de la orden
        lineItem.setOrderId(order.getOrderId());
        lineItem.setLineItemId(UUID.randomUUID());

        Order savedOrder = orderRepository.save(order);

        // 9. Publicar evento DESPUÉS del commit — si la DB falla, el evento no sale
        Promotion finalAppliedPromotion = appliedPromotion;
        publishAfterCommit(() -> {
            eventPublisher.publishOrderCreated(savedOrder);
            if (finalAppliedPromotion != null && !finalAppliedPromotion.isAvailable()) {
                eventPublisher.publishPromotionExhausted(finalAppliedPromotion);
            }
        });

        return savedOrder;
    }

    @Transactional(readOnly = true)
    @Override
    public Order getOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Order> getOrdersByBuyer(UUID buyerId, Pageable pageable) {
        return orderRepository.findByBuyerId(buyerId, pageable);
    }

    @Transactional
    @Override
    public void storeReservationSnapshot(ReservationSnapshot snapshot) {
        snapshotRepository.save(snapshot);
        log.info("Reservation snapshot stored: {}", snapshot.getReservationId());
    }

    @Transactional
    @Override
    public void expireOrderByReservation(UUID reservationId) {
        orderRepository.findByReservationId(reservationId).ifPresentOrElse(
                order -> {
                    order.expire();
                    Order expired = orderRepository.save(order);
                    // Publicar expiración después del commit
                    publishAfterCommit(() -> eventPublisher.publishOrderExpired(expired));
                    log.info("Order {} expired due to reservation {} expiry", order.getOrderId(), reservationId);
                },
                // Si no hay orden para esta reserva, solo log — no es error
                () -> log.warn("No order found for expired reservation {}, skipping", reservationId)
        );
    }

    @Transactional
    @Override
    public void confirmOrder(UUID orderId, UUID paymentId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        order.confirm();
        Order confirmed = orderRepository.save(order);
        // Publicar confirmación después del commit
        publishAfterCommit(() -> eventPublisher.publishOrderConfirmed(confirmed, paymentId));
        log.info("Order {} confirmed with payment {}", orderId, paymentId);
    }

    // Si hay transacción activa (producción), registra el hook afterCommit.
    // Si no (tests unitarios con Mockito), ejecuta inmediatamente para que los mocks sean verificables.
    private void publishAfterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }
}
