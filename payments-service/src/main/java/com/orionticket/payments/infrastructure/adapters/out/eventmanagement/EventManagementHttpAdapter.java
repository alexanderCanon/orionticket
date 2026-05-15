package com.orionticket.payments.infrastructure.adapters.out.eventmanagement;

import com.orionticket.payments.application.port.out.EventSummaryPort;
import com.orionticket.payments.domain.exception.PaymentNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.UUID;

/**
 * HTTP adapter for fetching Event data from the Event Management service.
 * Called during payout generation to resolve the organizerId for a given eventId.
 * Uses GET /v1/events/{eventId} — documented in service-contracts.md.
 */
@Component
public class EventManagementHttpAdapter implements EventSummaryPort {

    private static final Logger log = LoggerFactory.getLogger(EventManagementHttpAdapter.class);

    private final RestClient restClient;

    public EventManagementHttpAdapter(
            RestClient.Builder restClientBuilder,
            @Value("${services.event-management.base-url}") String eventMgmtBaseUrl) {
        this.restClient = restClientBuilder.baseUrl(eventMgmtBaseUrl).build();
    }

    @Override
    public EventSummary findByEventId(UUID eventId) {
        log.debug("Fetching event summary — eventId={}", eventId);
        try {
            EventResponse response = restClient.get()
                    .uri("/v1/events/{eventId}", eventId)
                    .retrieve()
                    .body(EventResponse.class);

            if (response == null) {
                throw new PaymentNotFoundException("Event Management returned empty response for eventId=" + eventId);
            }
            return new EventSummary(response.eventId(), response.organizerId(), response.status());

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new PaymentNotFoundException("Event not found. eventId=" + eventId);
            }
            log.error("Event Management service error — eventId={} status={}", eventId, e.getStatusCode());
            throw new com.orionticket.payments.domain.exception.PaymentGatewayException(
                    "Failed to fetch event from Event Management service: " + e.getMessage(), e);
        }
    }

    record EventResponse(UUID eventId, UUID organizerId, String name, String status) {}
}
