package com.orionticket.events.domain.port.out;

import com.orionticket.events.domain.model.Event;
import com.orionticket.events.domain.model.EventDate;

import java.util.UUID;

public interface EventPublisherPort {
    void publishEventCreated(Event event);
    void publishDateAdded(Event event, EventDate date);
    void publishVenueCreated(com.orionticket.events.domain.model.Venue venue);
    void publishEventSubmittedForReview(Event event, UUID submittedBy);
    void publishEventReleased(Event event, UUID operatorId);
    void publishEventRejected(Event event, UUID operatorId, String reason);
    void publishEventCanceled(Event event, UUID canceledBy, String reason);
    void publishDateCanceled(EventDate date, UUID canceledBy, String reason);
}
