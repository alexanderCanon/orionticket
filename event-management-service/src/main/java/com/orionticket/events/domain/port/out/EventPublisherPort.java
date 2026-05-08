package com.orionticket.events.domain.port.out;

import com.orionticket.events.domain.model.Event;
import com.orionticket.events.domain.model.EventDate;

public interface EventPublisherPort {
    void publishEventCreated(Event event);
    void publishDateAdded(Event event, EventDate date);
    void publishVenueCreated(com.orionticket.events.domain.model.Venue venue);
    void publishEventSubmittedForReview(Event event);
}
