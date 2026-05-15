package com.orionticket.acesscontrol.domain.port.out;

import com.orionticket.acesscontrol.domain.model.FailureReason;
import com.orionticket.acesscontrol.domain.model.ValidationResult;
import java.time.Instant;
import java.util.UUID;

public record TicketLookupResult(
        UUID ticketId,
        String status,
        String qrCode,
        Instant qrExpiresAt,
        String accessPolicy,
        UUID eventId,
        UUID dateId,
        ValidationResult result,
        FailureReason failureReason
) {
    public static TicketLookupResult success(UUID ticketId, String status, String qrCode,
            Instant qrExpiresAt, String accessPolicy, UUID eventId, UUID dateId) {
        return new TicketLookupResult(ticketId, status, qrCode, qrExpiresAt, accessPolicy,
                eventId, dateId, ValidationResult.SUCCEEDED, null);
    }

    public static TicketLookupResult failed(UUID ticketId, ValidationResult result,
            FailureReason failureReason) {
        return new TicketLookupResult(ticketId, null, null, null, null, null, null, result, failureReason);
    }

    public boolean isSuccess() {
        return result == ValidationResult.SUCCEEDED;
    }
}