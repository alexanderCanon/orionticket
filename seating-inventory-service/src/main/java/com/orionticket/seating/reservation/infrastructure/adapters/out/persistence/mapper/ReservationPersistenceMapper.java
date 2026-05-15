package com.orionticket.seating.reservation.infrastructure.adapters.out.persistence.mapper;

import com.orionticket.seating.reservation.domain.model.Reservation;
import com.orionticket.seating.reservation.infrastructure.adapters.out.persistence.entity.ReservationJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ReservationPersistenceMapper {

    public ReservationJpaEntity toEntity(Reservation reservation) {
        return ReservationJpaEntity.builder()
                .reservationId(reservation.getReservationId())
                .seatId(reservation.getSeatId())
                .buyerId(reservation.getBuyerId())
                .eventId(reservation.getEventId())
                .dateId(reservation.getDateId())
                .batchId(reservation.getBatchId())
                .expiresAt(reservation.getExpiresAt())
                .status(reservation.getStatus())
                .createdAt(reservation.getCreatedAt())
                .updatedAt(reservation.getUpdatedAt())
                .build();
    }

    public Reservation toDomain(ReservationJpaEntity entity) {
        return Reservation.builder()
                .reservationId(entity.getReservationId())
                .seatId(entity.getSeatId())
                .buyerId(entity.getBuyerId())
                .eventId(entity.getEventId())
                .dateId(entity.getDateId())
                .batchId(entity.getBatchId())
                .expiresAt(entity.getExpiresAt())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
