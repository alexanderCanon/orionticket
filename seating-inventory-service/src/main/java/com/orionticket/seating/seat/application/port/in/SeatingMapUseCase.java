package com.orionticket.seating.seat.application.port.in;

import com.orionticket.seating.seat.domain.model.Seat;

import java.util.List;
import java.util.UUID;

public interface SeatingMapUseCase {

    // Crea N asientos de admisión general vinculados a un batch.
    // Retorna la lista de asientos creados para que el Controller pueda armar la respuesta.
    List<Seat> configureGeneralAdmission(UUID eventId, UUID dateId, UUID batchId,
                                          int capacity, String accessPolicy);

    // Crea asientos numerados (MAPPED) a partir de una definición de filas y secciones.
    List<Seat> configureMappedSeats(UUID eventId, UUID dateId, UUID batchId,
                                     List<SeatDefinition> definitions);

    record SeatDefinition(String zone, String section, String rowLabel,
                          String seatNumber, String accessPolicy) {}
}
