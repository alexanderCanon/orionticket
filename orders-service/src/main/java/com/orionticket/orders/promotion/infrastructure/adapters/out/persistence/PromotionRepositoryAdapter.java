package com.orionticket.orders.promotion.infrastructure.adapters.out.persistence;

import com.orionticket.orders.promotion.domain.model.Promotion;
import com.orionticket.orders.promotion.domain.port.out.PromotionRepositoryPort;
import com.orionticket.orders.promotion.infrastructure.adapters.out.persistence.mapper.PromotionPersistenceMapper;
import com.orionticket.orders.promotion.infrastructure.adapters.out.persistence.repository.SpringDataPromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PromotionRepositoryAdapter implements PromotionRepositoryPort {

    private final SpringDataPromotionRepository repository;
    private final PromotionPersistenceMapper mapper;

    @Override
    public Promotion save(Promotion promotion) {
        return mapper.toDomain(repository.save(mapper.toEntity(promotion)));
    }

    @Override
    public Optional<Promotion> findById(UUID promotionId) {
        return repository.findById(promotionId).map(mapper::toDomain);
    }

    @Override
    public Optional<Promotion> findByCodeAndEventId(String code, UUID eventId) {
        return repository.findByCodeAndEventId(code, eventId).map(mapper::toDomain);
    }

    @Override
    public Optional<Promotion> findByIdWithLock(UUID promotionId) {
        // Delegamos a la query con @Lock(PESSIMISTIC_WRITE) — SELECT FOR UPDATE
        return repository.findByIdWithLock(promotionId).map(mapper::toDomain);
    }
}
