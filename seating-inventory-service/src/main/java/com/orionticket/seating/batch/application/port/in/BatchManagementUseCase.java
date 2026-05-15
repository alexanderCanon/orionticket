package com.orionticket.seating.batch.application.port.in;

import com.orionticket.seating.batch.domain.model.Batch;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

// Puerto de entrada: contrato que el Controller usa para invocar la lógica de negocio.
// Depender de esta interfaz (no del Service directamente) permite cambiar la implementación
// sin tocar el Controller, y facilita mockear el use case en tests del Controller.
public interface BatchManagementUseCase {

    Batch createBatch(UUID eventId, UUID dateId, String name,
                      BigDecimal price, String currency,
                      int capacity, ZonedDateTime scheduledStartAt);

    List<Batch> getBatchesByEventAndDate(UUID eventId, UUID dateId);
}
