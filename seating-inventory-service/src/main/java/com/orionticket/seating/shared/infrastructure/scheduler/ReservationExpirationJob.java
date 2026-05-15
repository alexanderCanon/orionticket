package com.orionticket.seating.shared.infrastructure.scheduler;

import com.orionticket.seating.batch.domain.model.Batch;
import com.orionticket.seating.batch.domain.port.out.BatchRepositoryPort;
import com.orionticket.seating.reservation.domain.model.Reservation;
import com.orionticket.seating.reservation.domain.port.out.DomainEventPublisherPort;
import com.orionticket.seating.reservation.domain.port.out.ReservationRepositoryPort;
import com.orionticket.seating.seat.domain.model.Seat;
import com.orionticket.seating.seat.domain.port.out.SeatRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

// Job que corre automáticamente cada 60 segundos para liberar reservas vencidas.
// @EnableScheduling en SeatingInventoryApplication activa el scheduler de Spring
// sin el cual estos métodos @Scheduled nunca se ejecutarían.
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationExpirationJob {

    private final ReservationRepositoryPort reservationRepository;
    private final SeatRepositoryPort seatRepository;
    private final BatchRepositoryPort batchRepository;
    private final DomainEventPublisherPort eventPublisher;

    // fixedDelay = 60_000 ms: espera 60 segundos DESPUÉS de que termina la ejecución anterior.
    // POR QUÉ fixedDelay y no fixedRate: si el job tarda 90 segundos en procesar un lote grande,
    // fixedRate dispararía otra ejecución DURANTE la primera (dos threads compitiendo por las mismas
    // filas). fixedDelay garantiza que siempre hay 60 segundos de pausa entre ejecuciones.
    //
    // POR QUÉ cada 60 segundos: las reservas expiran en 10 minutos. Con un ciclo de 60s,
    // el peor caso es que un asiento quede bloqueado 1 minuto extra después de vencer.
    // Más frecuente (e.g. 10s) consume CPU innecesariamente; menos frecuente (e.g. 5min)
    // dejaría asientos bloqueados demasiado tiempo afectando la experiencia de compra.
    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void expireReservations() {
        ZonedDateTime now = ZonedDateTime.now();
        List<Reservation> expiredList = reservationRepository.findActiveExpiredBefore(now);

        if (expiredList.isEmpty()) {
            return;
        }

        log.info("Expiration job: processing {} expired reservations", expiredList.size());

        for (Reservation reservation : expiredList) {
            try {
                // Procesamos cada reserva en el mismo contexto transaccional.
                // Si falla una, el @Transactional hace rollback de TODAS.
                // Para producción: cada expiración debería ser su propia TX (ver Outbox Pattern).
                Seat seat = seatRepository.findById(reservation.getSeatId())
                        .orElseThrow();

                Batch batch = batchRepository.findById(reservation.getBatchId())
                        .orElseThrow();

                // Cambiar estados en el dominio (lógica en los modelos, no aquí).
                reservation.expire();
                seat.release();

                // Decrementamos sold porque esta reserva nunca llegó a pago.
                // POR QUÉ decrementar: si no lo hacemos, el batch quedará en sold=X para
                // siempre aunque los asientos estén disponibles, bloqueando futuras compras.
                // Math.max(0, ...) en decrementSold() evita valores negativos por inconsistencia.
                batch.decrementSold();

                reservationRepository.save(reservation);
                seatRepository.save(seat);
                batchRepository.save(batch);

                // Publicar fuera de la transacción sería lo ideal, pero dentro de un loop
                // @Transactional publicamos después de cada save individual.
                // En Checkpoint 1 esto es aceptable; en producción usaríamos Outbox Pattern.
                eventPublisher.publishReservationExpired(reservation);

            } catch (Exception e) {
                log.error("Failed to expire reservation {}: {}", reservation.getReservationId(), e.getMessage());
                // No relanzamos la excepción para que el loop continúe con las demás reservas.
                // El @Transactional solo hace rollback si la excepción sale del método.
            }
        }

        log.info("Expiration job: completed. Processed {} reservations.", expiredList.size());
    }

    // Segundo job: activa batches cuyo scheduledStartAt ya llegó.
    // Corre con el mismo intervalo de 60 segundos.
    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void activateScheduledBatches() {
        List<com.orionticket.seating.batch.domain.model.Batch> dueBatches =
                batchRepository.findScheduledBatchesDue();

        for (com.orionticket.seating.batch.domain.model.Batch batch : dueBatches) {
            batch.activate();
            batchRepository.save(batch);
            eventPublisher.publishBatchActivated(batch);
            log.info("Batch {} activated", batch.getBatchId());
        }
    }
}
