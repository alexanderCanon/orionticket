package com.orionticket.seating.reservation.application.service;

import com.orionticket.seating.batch.domain.model.Batch;
import com.orionticket.seating.batch.domain.port.out.BatchRepositoryPort;
import com.orionticket.seating.reservation.domain.exception.BatchExhaustedException;
import com.orionticket.seating.reservation.domain.exception.ReservationConflictException;
import com.orionticket.seating.reservation.domain.model.Reservation;
import com.orionticket.seating.reservation.domain.port.out.DomainEventPublisherPort;
import com.orionticket.seating.reservation.domain.port.out.ReservationRepositoryPort;
import com.orionticket.seating.seat.domain.model.Seat;
import com.orionticket.seating.seat.domain.port.out.SeatRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock private SeatRepositoryPort seatRepository;
    @Mock private BatchRepositoryPort batchRepository;
    @Mock private ReservationRepositoryPort reservationRepository;
    @Mock private DomainEventPublisherPort eventPublisher;

    @InjectMocks
    private ReservationService reservationService;

    private UUID seatId;
    private UUID batchId;
    private UUID eventId;
    private UUID dateId;
    private UUID buyerId;

    private Seat availableSeat;
    private Batch activeBatch;

    @BeforeEach
    void setUp() {
        seatId  = UUID.randomUUID();
        batchId = UUID.randomUUID();
        eventId = UUID.randomUUID();
        dateId  = UUID.randomUUID();
        buyerId = UUID.randomUUID();

        availableSeat = Seat.createGeneralAdmission(eventId, dateId, batchId, null);

        activeBatch = Batch.create(eventId, dateId, "Test Batch",
                BigDecimal.valueOf(100), "GTQ", 50,
                ZonedDateTime.now().minusDays(1)); // scheduledStartAt en pasado → ACTIVE
    }

    @Test
    void createReservation_whenSeatAvailable_shouldSucceed() {
        when(seatRepository.findByIdWithLock(any())).thenReturn(Optional.of(availableSeat));
        when(batchRepository.findByIdWithLock(any())).thenReturn(Optional.of(activeBatch));
        when(reservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(seatRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(batchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Reservation result = reservationService.createReservation(
                seatId, buyerId, eventId, dateId, batchId);

        assertThat(result.getStatus()).isEqualTo("ACTIVE");
        assertThat(result.getExpiresAt()).isAfter(ZonedDateTime.now());
        assertThat(availableSeat.getStatus()).isEqualTo("RESERVED");
        assertThat(activeBatch.getSold()).isEqualTo(1);
        verify(eventPublisher).publishReservationCreated(result);
    }

    @Test
    void createReservation_whenSeatAlreadyReserved_shouldThrow409() {
        // Simulamos el estado que otro thread dejó tras hacer commit primero.
        availableSeat.reserve();

        when(seatRepository.findByIdWithLock(any())).thenReturn(Optional.of(availableSeat));

        assertThatThrownBy(() ->
                reservationService.createReservation(seatId, buyerId, eventId, dateId, batchId))
                .isInstanceOf(ReservationConflictException.class);

        // Ningún cambio en batch ni en reservations
        verifyNoInteractions(batchRepository, reservationRepository, eventPublisher);
    }

    @Test
    void createReservation_whenBatchExhausted_shouldThrow410() {
        when(seatRepository.findByIdWithLock(any())).thenReturn(Optional.of(availableSeat));

        // Batch con sold == capacity
        Batch exhaustedBatch = Batch.create(eventId, dateId, "Full",
                BigDecimal.ONE, "GTQ", 1, ZonedDateTime.now().minusDays(1));
        exhaustedBatch.incrementSold(); // sold=1, capacity=1 → EXHAUSTED

        when(batchRepository.findByIdWithLock(any())).thenReturn(Optional.of(exhaustedBatch));

        assertThatThrownBy(() ->
                reservationService.createReservation(seatId, buyerId, eventId, dateId, batchId))
                .isInstanceOf(BatchExhaustedException.class);

        verifyNoInteractions(reservationRepository, eventPublisher);
    }

    @Test
    void createReservation_whenBatchNotActive_shouldThrow410() {
        when(seatRepository.findByIdWithLock(any())).thenReturn(Optional.of(availableSeat));

        Batch scheduledBatch = Batch.create(eventId, dateId, "Scheduled",
                BigDecimal.ONE, "GTQ", 100,
                ZonedDateTime.now().plusDays(5)); // en el futuro → SCHEDULED

        when(batchRepository.findByIdWithLock(any())).thenReturn(Optional.of(scheduledBatch));

        assertThatThrownBy(() ->
                reservationService.createReservation(seatId, buyerId, eventId, dateId, batchId))
                .isInstanceOf(BatchExhaustedException.class);
    }
}
