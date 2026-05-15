package com.orionticket.seating.reservation.infrastructure.adapters.out.persistence;

import com.orionticket.seating.reservation.domain.model.Reservation;
import com.orionticket.seating.reservation.domain.port.out.ReservationRepositoryPort;
import com.orionticket.seating.reservation.infrastructure.adapters.out.persistence.mapper.ReservationPersistenceMapper;
import com.orionticket.seating.reservation.infrastructure.adapters.out.persistence.repository.SpringDataReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReservationRepositoryAdapter implements ReservationRepositoryPort {

    private final SpringDataReservationRepository springDataReservationRepository;
    private final ReservationPersistenceMapper mapper;

    @Override
    public Reservation save(Reservation reservation) {
        return mapper.toDomain(springDataReservationRepository.save(mapper.toEntity(reservation)));
    }

    @Override
    public Optional<Reservation> findById(UUID reservationId) {
        return springDataReservationRepository.findById(reservationId).map(mapper::toDomain);
    }

    @Override
    public List<Reservation> findActiveExpiredBefore(ZonedDateTime cutoff) {
        return springDataReservationRepository.findActiveExpiredBefore(cutoff)
                .stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<Reservation> findActiveByBuyerIdAndSeatId(UUID buyerId, UUID seatId) {
        return springDataReservationRepository.findActiveByBuyerIdAndSeatId(buyerId, seatId)
                .map(mapper::toDomain);
    }
}
