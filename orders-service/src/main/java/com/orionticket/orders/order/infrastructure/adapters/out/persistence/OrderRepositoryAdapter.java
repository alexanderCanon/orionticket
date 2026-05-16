package com.orionticket.orders.order.infrastructure.adapters.out.persistence;

import com.orionticket.orders.order.domain.model.Order;
import com.orionticket.orders.order.domain.port.out.OrderRepositoryPort;
import com.orionticket.orders.order.infrastructure.adapters.out.persistence.mapper.OrderPersistenceMapper;
import com.orionticket.orders.order.infrastructure.adapters.out.persistence.repository.SpringDataOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepositoryPort {

    private final SpringDataOrderRepository repository;
    private final OrderPersistenceMapper mapper;

    @Override
    public Order save(Order order) {
        return mapper.toDomain(repository.save(mapper.toEntity(order)));
    }

    @Override
    public Optional<Order> findById(UUID orderId) {
        return repository.findById(orderId).map(mapper::toDomain);
    }

    @Override
    public Optional<Order> findByReservationId(UUID reservationId) {
        return repository.findByReservationId(reservationId).map(mapper::toDomain);
    }

    @Override
    public Page<Order> findByBuyerId(UUID buyerId, Pageable pageable) {
        return repository.findByBuyerId(buyerId, pageable).map(mapper::toDomain);
    }
}
