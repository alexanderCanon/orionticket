package com.orionticket.seating.reservation.domain.port.out;

import com.orionticket.seating.batch.domain.model.Batch;
import com.orionticket.seating.reservation.domain.model.Reservation;

// Puerto de salida para publicar eventos de dominio al bus de mensajería.
// El dominio define QUÉ publicar; RabbitMqEventPublisherAdapter define CÓMO.
// Esta separación permite testear el dominio sin RabbitMQ real (usando un mock).
import java.math.BigDecimal;

public interface DomainEventPublisherPort {

    void publishReservationCreated(Reservation reservation, BigDecimal batchPrice);

    void publishReservationExpired(Reservation reservation);

    void publishReservationReleased(Reservation reservation);

    void publishBatchCreated(Batch batch);

    void publishBatchActivated(Batch batch);

    void publishBatchExhausted(Batch batch);
}
