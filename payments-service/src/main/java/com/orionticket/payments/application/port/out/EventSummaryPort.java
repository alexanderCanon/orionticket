package com.orionticket.payments.application.port.out;

import java.util.UUID;

/**
 * Output port for fetching Event data from the Event Management service.
 * Used by payout generation to resolve organizerId for a given eventId,
 * since DateAdded events do not include organizerId.
 */
public interface EventSummaryPort {

    /**
     * @param eventId the Event UUID
     * @return EventSummary with organizerId
     * @throws com.orionticket.payments.domain.exception.PaymentNotFoundException if not found
     */
    EventSummary findByEventId(UUID eventId);

    record EventSummary(UUID eventId, UUID organizerId, String status) {}
}
