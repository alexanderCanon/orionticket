package com.orionticket.events.application.service;

import com.orionticket.events.application.port.in.EventManagementUseCase;
import com.orionticket.events.domain.exception.EventNotFoundException;
import com.orionticket.events.domain.exception.UnauthorizedAccessException;
import com.orionticket.events.domain.model.Event;
import com.orionticket.events.domain.model.EventDate;
import com.orionticket.events.domain.port.out.EventPublisherPort;
import com.orionticket.events.domain.port.out.EventRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventManagementService implements EventManagementUseCase {

    private final EventRepositoryPort eventRepositoryPort;
    private final EventPublisherPort eventPublisherPort;

    @Override
    public Event createEvent(UUID organizerId, String name, String description) {
        Event event = Event.createDraft(organizerId, name, description);
        Event savedEvent = eventRepositoryPort.save(event);
        
        eventPublisherPort.publishEventCreated(savedEvent);
        return savedEvent;
    }

    @Override
    public EventDate addDateToEvent(UUID eventId, UUID organizerId, ZonedDateTime scheduledAt, UUID venueId, Integer capacity) {
        Event event = eventRepositoryPort.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found: " + eventId));

        if (!event.getOrganizerId().equals(organizerId)) {
            throw new UnauthorizedAccessException("You are not the owner of this event.");
        }

        EventDate newDate = event.addDate(scheduledAt, venueId, capacity);
        
        eventRepositoryPort.save(event);
        eventPublisherPort.publishDateAdded(event, newDate);
        
        return newDate;
    }

    @Override
    public Event submitEventForReview(UUID eventId, UUID organizerId) {
        Event event = eventRepositoryPort.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found: " + eventId));

        if (!event.getOrganizerId().equals(organizerId)) {
            throw new UnauthorizedAccessException("You are not the owner of this event.");
        }

        event.submitForReview();
        
        Event savedEvent = eventRepositoryPort.save(event);
        eventPublisherPort.publishEventSubmittedForReview(savedEvent);
        
        return savedEvent;
    }

    @Override
    public Event approveEvent(UUID eventId, UUID operatorId) {
        Event event = eventRepositoryPort.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found: " + eventId));

        // TODO: En el futuro, validar que operatorId tiene el rol PLATFORM_OPERATOR o SUPER_ADMIN
        
        event.approve();
        
        Event savedEvent = eventRepositoryPort.save(event);
        eventPublisherPort.publishEventReleased(savedEvent, operatorId);
        
        return savedEvent;
    }

    @Override
    public Event rejectEvent(UUID eventId, UUID operatorId, String reason) {
        Event event = eventRepositoryPort.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found: " + eventId));

        // TODO: En el futuro, validar que operatorId tiene el rol PLATFORM_OPERATOR o SUPER_ADMIN
        
        event.reject(reason);
        
        Event savedEvent = eventRepositoryPort.save(event);
        eventPublisherPort.publishEventRejected(savedEvent, operatorId, reason);
        
        return savedEvent;
    }
}
