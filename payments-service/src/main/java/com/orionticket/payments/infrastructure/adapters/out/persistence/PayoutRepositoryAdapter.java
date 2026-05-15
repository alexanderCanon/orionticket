package com.orionticket.payments.infrastructure.adapters.out.persistence;

import com.orionticket.payments.domain.model.Payout;
import com.orionticket.payments.domain.port.out.PayoutRepositoryPort;
import com.orionticket.payments.infrastructure.adapters.out.persistence.mapper.PayoutMapper;
import com.orionticket.payments.infrastructure.adapters.out.persistence.repository.SpringDataPayoutRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class PayoutRepositoryAdapter implements PayoutRepositoryPort {

    private final SpringDataPayoutRepository repository;
    private final PayoutMapper mapper;

    public PayoutRepositoryAdapter(SpringDataPayoutRepository repository, PayoutMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Payout save(Payout payout) {
        return mapper.toDomain(repository.save(mapper.toEntity(payout)));
    }

    @Override
    public Optional<Payout> findById(UUID payoutId) {
        return repository.findById(payoutId).map(mapper::toDomain);
    }

    @Override
    public List<Payout> findByOrganizerId(UUID organizerId) {
        return repository.findByOrganizerId(organizerId).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Payout> findByOrganizerIdAndStatus(UUID organizerId, Payout.PayoutStatus status) {
        return repository.findByOrganizerIdAndStatus(organizerId,
            com.orionticket.payments.infrastructure.adapters.out.persistence.entity.PayoutJpaEntity.PayoutStatus.valueOf(status.name())
        ).stream().map(mapper::toDomain).collect(Collectors.toList());
    }
}