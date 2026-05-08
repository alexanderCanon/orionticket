package com.orionticket.events.domain.port.out;

import com.orionticket.events.domain.model.Event;
import com.orionticket.events.domain.model.EventDate;

import java.util.UUID;

public interface EventPublisherPort {
    void publishEventCreated(Event event);
    void publishDateAdded(Event event, EventDate date);
    void publishVenueCreated(com.orionticket.events.domain.model.Venue venue);
    void publishEventSubmittedForReview(Event event);
    void publishEventReleased(Event event, UUID operatorId);
    void publishEventRejected(Event event, UUID operatorId, String reason);
}
