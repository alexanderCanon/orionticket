package com.orionticket.events.application;

import com.orionticket.events.application.service.EventManagementService;
import com.orionticket.events.domain.exception.EventNotFoundException;
import com.orionticket.events.domain.exception.UnauthorizedAccessException;
import com.orionticket.events.domain.model.Event;
import com.orionticket.events.domain.model.EventDate;
import com.orionticket.events.domain.port.out.EventPublisherPort;
import com.orionticket.events.domain.port.out.EventRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EventManagementServiceTest {

    @Mock
    private EventRepositoryPort eventRepositoryPort;

    @Mock
    private EventPublisherPort eventPublisherPort;

    @InjectMocks
    private EventManagementService eventManagementService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreateEventSuccessfully() {
        UUID organizerId = UUID.randomUUID();
        when(eventRepositoryPort.save(any(Event.class))).thenAnswer(i -> i.getArguments()[0]);

        Event event = eventManagementService.createEvent(organizerId, "My Event", "Description");

        assertNotNull(event.getEventId());
        assertEquals("DRAFT", event.getStatus());
        assertEquals(organizerId, event.getOrganizerId());

        verify(eventRepositoryPort, times(1)).save(event);
        verify(eventPublisherPort, times(1)).publishEventCreated(event);
    }

    @Test
    void shouldAddDateToEventSuccessfully() {
        UUID organizerId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        Event event = Event.createDraft(organizerId, "My Event", "Desc");
        event.setEventId(eventId);

        when(eventRepositoryPort.findById(eventId)).thenReturn(Optional.of(event));
        when(eventRepositoryPort.save(any(Event.class))).thenAnswer(i -> i.getArguments()[0]);

        UUID venueId = UUID.randomUUID();
        ZonedDateTime date = ZonedDateTime.now().plusDays(10);
        EventDate addedDate = eventManagementService.addDateToEvent(eventId, organizerId, date, venueId, 100);

        assertNotNull(addedDate.getDateId());
        assertEquals(1, event.getDates().size());
        assertEquals(venueId, addedDate.getVenueId());

        verify(eventRepositoryPort, times(1)).save(event);
        verify(eventPublisherPort, times(1)).publishDateAdded(event, addedDate);
    }

    @Test
    void shouldThrowUnauthorizedWhenAddingDateByDifferentOrganizer() {
        UUID organizerId = UUID.randomUUID();
        UUID attackerId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        Event event = Event.createDraft(organizerId, "My Event", "Desc");
        event.setEventId(eventId);

        when(eventRepositoryPort.findById(eventId)).thenReturn(Optional.of(event));

        assertThrows(UnauthorizedAccessException.class, () -> {
            eventManagementService.addDateToEvent(eventId, attackerId, ZonedDateTime.now(), UUID.randomUUID(), 100);
        });

        verify(eventRepositoryPort, never()).save(any());
        verify(eventPublisherPort, never()).publishDateAdded(any(), any());
    }

    @Test
    void shouldThrowNotFoundWhenAddingDateToNonExistentEvent() {
        UUID eventId = UUID.randomUUID();
        when(eventRepositoryPort.findById(eventId)).thenReturn(Optional.empty());

        assertThrows(EventNotFoundException.class, () -> {
            eventManagementService.addDateToEvent(eventId, UUID.randomUUID(), ZonedDateTime.now(), UUID.randomUUID(), 100);
        });
    }

    @Test
    void shouldSubmitEventForReviewSuccessfully() {
        UUID organizerId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        Event event = Event.createDraft(organizerId, "My Event", "Desc");
        event.setEventId(eventId);
        event.addDate(ZonedDateTime.now().plusDays(10), UUID.randomUUID(), 100);

        when(eventRepositoryPort.findById(eventId)).thenReturn(java.util.Optional.of(event));
        when(eventRepositoryPort.save(any(Event.class))).thenAnswer(i -> i.getArguments()[0]);

        Event submittedEvent = eventManagementService.submitEventForReview(eventId, organizerId);

        assertEquals("UNDER_REVIEW", submittedEvent.getStatus());
        verify(eventRepositoryPort, times(1)).save(event);
        verify(eventPublisherPort, times(1)).publishEventSubmittedForReview(event);
    }

    @Test
    void shouldThrowExceptionWhenSubmittingEventWithoutDates() {
        UUID organizerId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        Event event = Event.createDraft(organizerId, "My Event", "Desc");
        event.setEventId(eventId);
        // Note: No dates added

        when(eventRepositoryPort.findById(eventId)).thenReturn(java.util.Optional.of(event));

        assertThrows(com.orionticket.events.domain.exception.InvalidEventStateException.class, () -> {
            eventManagementService.submitEventForReview(eventId, organizerId);
        });

        verify(eventRepositoryPort, never()).save(any());
        verify(eventPublisherPort, never()).publishEventSubmittedForReview(any());
    }
}
