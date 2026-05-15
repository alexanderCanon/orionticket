package com.orionticket.payments.application.port.in;

import com.orionticket.payments.domain.model.Payout;

import java.util.List;
import java.util.UUID;

public interface ManagePayoutsUseCase {

    List<Payout> listPayouts(UUID organizerId, Payout.PayoutStatus status, int page, int size);

    Payout getPayout(UUID payoutId);
}