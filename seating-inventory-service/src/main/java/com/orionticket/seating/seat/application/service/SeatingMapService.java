package com.orionticket.seating.seat.application.service;

import com.orionticket.seating.reservation.domain.port.out.DomainEventPublisherPort;
import com.orionticket.seating.seat.application.port.in.SeatAvailabilityUseCase;
import com.orionticket.seating.seat.application.port.in.SeatingMapUseCase;
import com.orionticket.seating.seat.domain.model.Seat;
import com.orionticket.seating.seat.domain.port.out.SeatRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class SeatingMapService implements SeatingMapUseCase, SeatAvailabilityUseCase {

    private final SeatRepositoryPort seatRepository;
    private final DomainEventPublisherPort eventPublisher;

    @Override
    @Transactional
    public List<Seat> configureGeneralAdmission(UUID eventId, UUID dateId, UUID batchId,
                                                 int capacity, String accessPolicy) {
        // Crea `capacity` asientos GA genéricos en una sola transacción.
        // saveAll usa un batch INSERT de JPA para no hacer N queries individuales.
        List<Seat> seats = IntStream.range(0, capacity)
                .mapToObj(i -> Seat.createGeneralAdmission(eventId, dateId, batchId, accessPolicy))
                .collect(Collectors.toList());

        return seatRepository.saveAll(seats);
    }

    @Override
    @Transactional
    public List<Seat> configureMappedSeats(UUID eventId, UUID dateId, UUID batchId,
                                            List<SeatDefinition> definitions) {
        List<Seat> seats = definitions.stream()
                .map(d -> Seat.createMapped(eventId, dateId, batchId,
                        d.zone(), d.section(), d.rowLabel(), d.seatNumber(), d.accessPolicy()))
                .collect(Collectors.toList());

        return seatRepository.saveAll(seats);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Seat> getAvailableSeats(UUID eventId, UUID dateId, String zone, String section) {
        if (zone != null && section != null) {
            return seatRepository.findByEventIdAndDateIdAndZoneAndSection(eventId, dateId, zone, section);
        }
        if (zone != null) {
            return seatRepository.findByEventIdAndDateIdAndZone(eventId, dateId, zone);
        }
        return seatRepository.findByEventIdAndDateId(eventId, dateId);
    }
}
