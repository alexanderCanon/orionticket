package com.orionticket.payments.infrastructure.adapters.out.orders;

import com.orionticket.payments.application.port.out.OrderSummaryPort;
import com.orionticket.payments.domain.exception.PaymentNotFoundException;
import com.orionticket.payments.infrastructure.adapters.out.persistence.entity.OrderProjectionEntity;
import com.orionticket.payments.infrastructure.adapters.out.persistence.repository.SpringDataOrderProjectionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Primary implementation of OrderSummaryPort backed by the local order projection.
 * Eliminates synchronous HTTP calls to the Orders service during payment initiation.
 * The projection is populated by OrderCreatedConsumer.
 *
 * Falls back with PaymentNotFoundException if the projection is not yet available
 * (e.g. OrderCreated event not yet received). In that case, the client should retry.
 *
 * Marked @Primary so Spring injects this over OrderServiceHttpAdapter.
 */
@Primary
@Component
public class OrderProjectionAdapter implements OrderSummaryPort {

    private static final Logger log = LoggerFactory.getLogger(OrderProjectionAdapter.class);

    private final SpringDataOrderProjectionRepository repository;

    public OrderProjectionAdapter(SpringDataOrderProjectionRepository repository) {
        this.repository = repository;
    }

    @Override
    public OrderSummary findByOrderId(UUID orderId) {
        log.debug("Looking up order projection — orderId={}", orderId);

        OrderProjectionEntity entity = repository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("Order projection not found — orderId={}. OrderCreated event may not have arrived yet.",
                            orderId);
                    return new PaymentNotFoundException(
                            "Order not found in local projection. orderId=" + orderId +
                            ". Retry after a moment — the OrderCreated event may still be in transit.");
                });

        return new OrderSummary(
                entity.getOrderId(),
                entity.getBuyerId(),
                entity.getTotal(),
                entity.getServiceFee(),
                entity.getCurrency(),
                entity.getStatus()
        );
    }
}
