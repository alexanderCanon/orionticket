package com.orionticket.orders.promotion.domain.port.out;

import com.orionticket.orders.promotion.domain.model.Promotion;

import java.util.Optional;
import java.util.UUID;

public interface PromotionRepositoryPort {
    Promotion save(Promotion promotion);
    Optional<Promotion> findById(UUID promotionId);
    // Buscar por código + evento para validar al crear la orden
    Optional<Promotion> findByCodeAndEventId(String code, UUID eventId);
    // SELECT FOR UPDATE — lock pesimista para prevenir race condition en uso concurrente del código
    Optional<Promotion> findByIdWithLock(UUID promotionId);
}
