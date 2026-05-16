package com.orionticket.orders.order.infrastructure.adapters.out.persistence;

import com.orionticket.orders.order.domain.model.ReservationSnapshot;
import com.orionticket.orders.order.domain.port.out.ReservationSnapshotRepositoryPort;
import com.orionticket.orders.order.infrastructure.adapters.out.persistence.mapper.OrderPersistenceMapper;
import com.orionticket.orders.order.infrastructure.adapters.out.persistence.repository.SpringDataReservationSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReservationSnapshotRepositoryAdapter implements ReservationSnapshotRepositoryPort {

    private final SpringDataReservationSnapshotRepository repository;
    private final OrderPersistenceMapper mapper;

    @Override
    public ReservationSnapshot save(ReservationSnapshot snapshot) {
        return mapper.toSnapshotDomain(repository.save(mapper.toSnapshotEntity(snapshot)));
    }

    @Override
    public Optional<ReservationSnapshot> findById(UUID reservationId) {
        return repository.findById(reservationId).map(mapper::toSnapshotDomain);
    }
}
