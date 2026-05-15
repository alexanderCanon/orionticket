package com.orionticket.payments.domain.port.out;

import com.orionticket.payments.domain.model.Payout;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PayoutRepositoryPort {

    Payout save(Payout payout);

    Optional<Payout> findById(UUID payoutId);

    List<Payout> findByOrganizerId(UUID organizerId);

    List<Payout> findByOrganizerIdAndStatus(UUID organizerId, Payout.PayoutStatus status);
}