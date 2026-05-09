package com.orionticket.acesscontrol.infrastructure.adapters.out.persistence.repository;

import com.orionticket.acesscontrol.infrastructure.adapters.out.persistence.entity.ValidationRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ValidationRecordJpaRepository extends JpaRepository<ValidationRecordEntity, UUID> {

    Optional<ValidationRecordEntity> findFirstByTicketIdAndResult(UUID ticketId, String result);

    boolean existsByTicketIdAndResult(UUID ticketId, String result);

    List<ValidationRecordEntity> findByEventIdAndDateId(UUID eventId, UUID dateId);
}