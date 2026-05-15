package com.orionticket.seating.reservation.application.service;

import com.orionticket.seating.batch.domain.exception.BatchNotFoundException;
import com.orionticket.seating.batch.domain.model.Batch;
import com.orionticket.seating.batch.domain.port.out.BatchRepositoryPort;
import com.orionticket.seating.reservation.application.port.in.ReservationUseCase;
import com.orionticket.seating.reservation.domain.exception.BatchExhaustedException;
import com.orionticket.seating.reservation.domain.exception.ReservationConflictException;
import com.orionticket.seating.reservation.domain.exception.ReservationNotFoundException;
import com.orionticket.seating.reservation.domain.model.Reservation;
import com.orionticket.seating.reservation.domain.port.out.DomainEventPublisherPort;
import com.orionticket.seating.reservation.domain.port.out.ReservationRepositoryPort;
import com.orionticket.seating.seat.domain.exception.SeatNotFoundException;
import com.orionticket.seating.seat.domain.model.Seat;
import com.orionticket.seating.seat.domain.port.out.SeatRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationService implements ReservationUseCase {

    private final SeatRepositoryPort seatRepository;
    private final BatchRepositoryPort batchRepository;
    private final ReservationRepositoryPort reservationRepository;
    private final DomainEventPublisherPort eventPublisher;

    // @Transactional: TODO el método corre en una sola transacción de base de datos.
    // Si cualquier paso falla (excepción), PostgreSQL hace rollback automático de todos
    // los cambios: el asiento no queda en RESERVED, el batch no queda incrementado,
    // y la reserva no se guarda. No hay estados intermedios inconsistentes.
    @Override
    @Transactional
    public Reservation createReservation(UUID seatId, UUID buyerId,
                                         UUID eventId, UUID dateId, UUID batchId) {

        // PASO 1: findByIdWithLock emite "SELECT * FROM seats WHERE seat_id = ? FOR UPDATE".
        // FOR UPDATE bloquea la fila en PostgreSQL hasta que esta transacción termine.
        // Si otra transacción concurrente intenta el mismo lock, ESPERA (no falla).
        // Esto garantiza que dos requests simultáneos para el mismo asiento se serialicen:
        // uno obtiene el lock, el otro espera; cuando el primero hace commit y libera el lock,
        // el segundo ve el estado actualizado (RESERVED) y lanza 409.
        Seat seat = seatRepository.findByIdWithLock(seatId)
                .orElseThrow(() -> new SeatNotFoundException(seatId));

        // PASO 2: verificar disponibilidad DESPUÉS de obtener el lock.
        // POR QUÉ después: si verificamos antes del lock, dos threads pueden leer
        // "AVAILABLE" al mismo tiempo y ambos pasar la validación, causando overbooking.
        // Con el lock, solo un thread puede leer y escribir a la vez.
        if (!seat.isAvailable()) {
            throw new ReservationConflictException(seatId);
        }

        // PASO 3: lock sobre el batch también, para que batch.sold no se lea concurrentemente.
        // Si dos reservas del mismo batch ocurren al mismo tiempo:
        //   - Thread A obtiene lock del seat A, luego lock del batch
        //   - Thread B obtiene lock del seat B, luego espera el lock del batch
        // Sin este lock, ambos leerían sold=99 de un batch con capacity=100 y ambos pasarían.
        Batch batch = batchRepository.findByIdWithLock(batchId)
                .orElseThrow(() -> new BatchNotFoundException(batchId));

        // PASO 4: el batch debe estar ACTIVE (no SCHEDULED, EXHAUSTED ni EXPIRED).
        if (!batch.isActive()) {
            throw new BatchExhaustedException(batchId);
        }

        // PASO 5: verificar que el batch aún tiene capacidad.
        if (batch.isExhausted()) {
            throw new BatchExhaustedException(batchId);
        }

        // PASO 6: crear la reserva con expiresAt = ahora + 10 minutos.
        Reservation reservation = Reservation.create(seatId, buyerId, eventId, dateId, batchId);

        // PASO 7: actualizar estado del asiento → RESERVED.
        seat.reserve();

        // PASO 8: incrementar sold del batch. Si sold llega a capacity,
        // el método interno cambia status a EXHAUSTED y publicaremos BatchExhausted.
        batch.incrementSold();

        // PASO 9-11: persistir los tres cambios dentro de la misma transacción.
        // Spring JPA los agrupa en el mismo flush para minimizar round-trips a la DB.
        reservationRepository.save(reservation);
        seatRepository.save(seat);
        batchRepository.save(batch);

        // AQUÍ ocurre el COMMIT de la transacción (al salir del método @Transactional).

        // PASO 12: publicar el evento DESPUÉS del commit.
        // POR QUÉ publicar fuera de la transacción: si publicamos antes del commit y
        // luego el commit falla (error de red, constraint violation), Orders Service ya
        // recibió un ReservationCreated de una reserva que no existe en la DB.
        // Al publicar después del commit, garantizamos consistencia eventual:
        // si el publish falla (RabbitMQ caído), la reserva SÍ está en DB pero el evento
        // no se envió. Esto es aceptable para el Checkpoint 1 (Outbox Pattern lo resolvería completamente).
        eventPublisher.publishReservationCreated(reservation);

        if (batch.isExhausted()) {
            eventPublisher.publishBatchExhausted(batch);
        }

        return reservation;
    }

    @Override
    @Transactional
    public Reservation releaseReservation(UUID reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        Seat seat = seatRepository.findByIdWithLock(reservation.getSeatId())
                .orElseThrow(() -> new SeatNotFoundException(reservation.getSeatId()));

        Batch batch = batchRepository.findByIdWithLock(reservation.getBatchId())
                .orElseThrow(() -> new BatchNotFoundException(reservation.getBatchId()));

        reservation.release();
        seat.release();
        batch.decrementSold();

        reservationRepository.save(reservation);
        seatRepository.save(seat);
        batchRepository.save(batch);

        // Publicar después del commit, por las mismas razones que en createReservation.
        eventPublisher.publishReservationReleased(reservation);

        return reservation;
    }
}
