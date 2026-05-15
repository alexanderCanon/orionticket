package com.orionticket.seating.seat.infrastructure.adapters.out.persistence;

import com.orionticket.seating.batch.infrastructure.adapters.out.persistence.entity.BatchJpaEntity;
import com.orionticket.seating.batch.infrastructure.adapters.out.persistence.repository.SpringDataBatchRepository;
import com.orionticket.seating.seat.domain.model.Seat;
import com.orionticket.seating.seat.domain.port.out.SeatRepositoryPort;
import com.orionticket.seating.seat.infrastructure.adapters.out.persistence.entity.SeatJpaEntity;
import com.orionticket.seating.seat.infrastructure.adapters.out.persistence.mapper.SeatPersistenceMapper;
import com.orionticket.seating.seat.infrastructure.adapters.out.persistence.repository.SpringDataSeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SeatRepositoryAdapter implements SeatRepositoryPort {

    private final SpringDataSeatRepository springDataSeatRepository;
    private final SpringDataBatchRepository springDataBatchRepository;
    private final SeatPersistenceMapper mapper;

    @Override
    public Seat save(Seat seat) {
        BatchJpaEntity batchEntity = springDataBatchRepository.getReferenceById(seat.getBatchId());
        return mapper.toDomain(springDataSeatRepository.save(mapper.toEntity(seat, batchEntity)));
    }

    @Override
    public List<Seat> saveAll(List<Seat> seats) {
        List<SeatJpaEntity> entities = seats.stream().map(seat -> {
            BatchJpaEntity batchEntity = springDataBatchRepository.getReferenceById(seat.getBatchId());
            return mapper.toEntity(seat, batchEntity);
        }).collect(Collectors.toList());
        return springDataSeatRepository.saveAll(entities).stream()
                .map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<Seat> findById(UUID seatId) {
        return springDataSeatRepository.findById(seatId).map(mapper::toDomain);
    }

    @Override
    public Optional<Seat> findByIdWithLock(UUID seatId) {
        return springDataSeatRepository.findByIdWithLock(seatId).map(mapper::toDomain);
    }

    @Override
    public List<Seat> findByEventIdAndDateId(UUID eventId, UUID dateId) {
        return springDataSeatRepository.findByEventIdAndDateId(eventId, dateId)
                .stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Seat> findByEventIdAndDateIdAndZone(UUID eventId, UUID dateId, String zone) {
        return springDataSeatRepository.findByEventIdAndDateIdAndZone(eventId, dateId, zone)
                .stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Seat> findByEventIdAndDateIdAndZoneAndSection(UUID eventId, UUID dateId,
                                                               String zone, String section) {
        return springDataSeatRepository.findByEventIdAndDateIdAndZoneAndSection(eventId, dateId, zone, section)
                .stream().map(mapper::toDomain).collect(Collectors.toList());
    }
}
