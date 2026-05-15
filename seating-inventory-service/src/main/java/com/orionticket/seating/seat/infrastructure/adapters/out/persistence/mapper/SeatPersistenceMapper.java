package com.orionticket.seating.seat.infrastructure.adapters.out.persistence.mapper;

import com.orionticket.seating.batch.infrastructure.adapters.out.persistence.entity.BatchJpaEntity;
import com.orionticket.seating.seat.domain.model.Seat;
import com.orionticket.seating.seat.infrastructure.adapters.out.persistence.entity.SeatJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class SeatPersistenceMapper {

    // toEntity necesita la BatchJpaEntity completa para satisfacer la FK @ManyToOne.
    // La obtenemos en el adapter antes de llamar este mapper.
    public SeatJpaEntity toEntity(Seat seat, BatchJpaEntity batchEntity) {
        return SeatJpaEntity.builder()
                .seatId(seat.getSeatId())
                .eventId(seat.getEventId())
                .dateId(seat.getDateId())
                .batch(batchEntity)
                .zone(seat.getZone())
                .section(seat.getSection())
                .rowLabel(seat.getRowLabel())
                .seatNumber(seat.getSeatNumber())
                .type(seat.getType())
                .status(seat.getStatus())
                .accessPolicy(seat.getAccessPolicy())
                .createdAt(seat.getCreatedAt())
                .updatedAt(seat.getUpdatedAt())
                .build();
    }

    public Seat toDomain(SeatJpaEntity entity) {
        return Seat.builder()
                .seatId(entity.getSeatId())
                .eventId(entity.getEventId())
                .dateId(entity.getDateId())
                .batchId(entity.getBatch().getBatchId())
                .zone(entity.getZone())
                .section(entity.getSection())
                .rowLabel(entity.getRowLabel())
                .seatNumber(entity.getSeatNumber())
                .type(entity.getType())
                .status(entity.getStatus())
                .accessPolicy(entity.getAccessPolicy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
