package com.orionticket.events.application.port.in;

import com.orionticket.events.domain.model.Event;
import com.orionticket.events.domain.model.EventDate;

import java.time.ZonedDateTime;
import java.util.UUID;

public interface EventManagementUseCase {
    Event createEvent(UUID organizerId, String name, String description);
    EventDate addDateToEvent(UUID eventId, UUID organizerId, ZonedDateTime scheduledAt, UUID venueId, Integer capacity);
    Event submitEventForReview(UUID eventId, UUID organizerId);
    Event approveEvent(UUID eventId, UUID operatorId);
    Event rejectEvent(UUID eventId, UUID operatorId, String reason);
}
