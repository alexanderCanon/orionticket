package com.orionticket.events.application;

import com.orionticket.events.application.service.VenueManagementService;
import com.orionticket.events.domain.model.Venue;
import com.orionticket.events.domain.port.out.EventPublisherPort;
import com.orionticket.events.domain.port.out.VenueRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VenueManagementServiceTest {

    @Mock
    private VenueRepositoryPort venueRepositoryPort;

    @Mock
    private EventPublisherPort eventPublisherPort;

    @InjectMocks
    private VenueManagementService venueManagementService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreateVenueSuccessfully() {
        UUID organizerId = UUID.randomUUID();
        when(venueRepositoryPort.save(any(Venue.class))).thenAnswer(i -> i.getArguments()[0]);

        Venue venue = venueManagementService.createVenue(organizerId, "Stadium", "123 Street", 50000);

        assertNotNull(venue.getVenueId());
        assertEquals("Stadium", venue.getName());
        assertEquals(organizerId, venue.getOrganizerId());

        verify(venueRepositoryPort, times(1)).save(venue);
        verify(eventPublisherPort, times(1)).publishVenueCreated(venue);
    }

    @Test
    void shouldThrowExceptionWhenCapacityIsInvalid() {
        UUID organizerId = UUID.randomUUID();
        
        assertThrows(IllegalArgumentException.class, () -> {
            venueManagementService.createVenue(organizerId, "Stadium", "123 Street", 0);
        });

        verify(venueRepositoryPort, never()).save(any());
        verify(eventPublisherPort, never()).publishVenueCreated(any());
    }

    @Test
    void shouldGetVenuesByOrganizer() {
        UUID organizerId = UUID.randomUUID();
        Venue v1 = Venue.create(organizerId, "V1", "A1", 100);
        Venue v2 = Venue.create(organizerId, "V2", "A2", 200);
        
        when(venueRepositoryPort.findAllByOrganizerId(organizerId)).thenReturn(List.of(v1, v2));
        
        List<Venue> venues = venueManagementService.getVenuesByOrganizer(organizerId);
        assertEquals(2, venues.size());
    }
}
