package com.orionticket.seating.batch.infrastructure.adapters.out.persistence.mapper;

import com.orionticket.seating.batch.domain.model.Batch;
import com.orionticket.seating.batch.infrastructure.adapters.out.persistence.entity.BatchJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class BatchPersistenceMapper {

    public BatchJpaEntity toEntity(Batch batch) {
        return BatchJpaEntity.builder()
                .batchId(batch.getBatchId())
                .eventId(batch.getEventId())
                .dateId(batch.getDateId())
                .name(batch.getName())
                .price(batch.getPrice())
                .currency(batch.getCurrency())
                .capacity(batch.getCapacity())
                .sold(batch.getSold())
                .status(batch.getStatus())
                .scheduledStartAt(batch.getScheduledStartAt())
                .createdAt(batch.getCreatedAt())
                .updatedAt(batch.getUpdatedAt())
                .build();
    }

    public Batch toDomain(BatchJpaEntity entity) {
        return Batch.builder()
                .batchId(entity.getBatchId())
                .eventId(entity.getEventId())
                .dateId(entity.getDateId())
                .name(entity.getName())
                .price(entity.getPrice())
                .currency(entity.getCurrency())
                .capacity(entity.getCapacity())
                .sold(entity.getSold())
                .status(entity.getStatus())
                .scheduledStartAt(entity.getScheduledStartAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
