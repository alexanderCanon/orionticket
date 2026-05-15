package com.orionticket.payments.infrastructure.adapters.out.persistence.mapper;

import com.orionticket.payments.domain.model.Payout;
import com.orionticket.payments.infrastructure.adapters.out.persistence.entity.PayoutJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class PayoutMapper {

    public Payout toDomain(PayoutJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Payout(
            entity.getPayoutId(),
            entity.getOrganizerId(),
            entity.getEventId(),
            entity.getDateId(),
            entity.getGrossAmount(),
            entity.getServiceFeeTotal(),
            entity.getNetAmount(),
            Payout.PayoutStatus.valueOf(entity.getStatus().name()),
            entity.getRetryCount(),
            entity.getTriggeredAt(),
            entity.getProcessedAt()
        );
    }

    public PayoutJpaEntity toEntity(Payout domain) {
        if (domain == null) {
            return null;
        }
        PayoutJpaEntity entity = new PayoutJpaEntity();
        entity.setPayoutId(domain.getPayoutId());
        entity.setOrganizerId(domain.getOrganizerId());
        entity.setEventId(domain.getEventId());
        entity.setDateId(domain.getDateId());
        entity.setGrossAmount(domain.getGrossAmount());
        entity.setServiceFeeTotal(domain.getServiceFeeTotal());
        entity.setNetAmount(domain.getNetAmount());
        entity.setStatus(PayoutJpaEntity.PayoutStatus.valueOf(domain.getStatus().name()));
        entity.setRetryCount(domain.getRetryCount());
        entity.setTriggeredAt(domain.getTriggeredAt());
        entity.setProcessedAt(domain.getProcessedAt());
        return entity;
    }
}