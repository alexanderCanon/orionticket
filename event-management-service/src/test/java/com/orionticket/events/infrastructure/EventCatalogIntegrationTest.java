package com.orionticket.events.infrastructure;

import com.orionticket.events.domain.model.Event;
import com.orionticket.events.domain.port.out.EventRepositoryPort;
import com.orionticket.events.domain.port.out.VenueRepositoryPort;
import com.orionticket.events.domain.model.Venue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@Transactional
public class EventCatalogIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventRepositoryPort eventRepositoryPort;

    @Autowired
    private VenueRepositoryPort venueRepositoryPort;

    private UUID organizerId;
    private UUID venueId;

    @BeforeEach
    void setUp() {
        organizerId = UUID.randomUUID();
        
        Venue venue1 = Venue.builder()
                .venueId(UUID.randomUUID())
                .organizerId(organizerId)
                .name("Estadio Nacional")
                .address("Calle 1")
                .city("Guatemala")
                .capacity(1000)
                .build();
        venueRepositoryPort.save(venue1);
        
        Venue venue2 = Venue.builder()
                .venueId(UUID.randomUUID())
                .organizerId(organizerId)
                .name("Teatro Abril")
                .address("Calle 2")
                .city("Quetzaltenango")
                .capacity(500)
                .build();
        venueRepositoryPort.save(venue2);

        // Evento 1: RELEASED (Publicado) en Guatemala
        Event event1 = Event.createDraft(organizerId, "Concierto Rock", "Descripción", "Music");
        event1.setOrganizerName("Conciertos GT");
        event1.addDate(ZonedDateTime.now().plusDays(10), venue1.getVenueId(), 500);
        event1.setStatus("RELEASED");
        eventRepositoryPort.save(event1);

        // Evento 2: RELEASED (Publicado) en Quetzaltenango
        Event event2 = Event.createDraft(organizerId, "Teatro Clásico", "Descripción", "Arts");
        event2.setOrganizerName("Teatro GT");
        event2.addDate(ZonedDateTime.now().plusDays(5), venue2.getVenueId(), 200);
        event2.setStatus("RELEASED");
        eventRepositoryPort.save(event2);
    }

    @Test
    void shouldReturnOnlyReleasedEvents() throws Exception {
        mockMvc.perform(get("/v1/catalog/events")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events.length()").value(2))
                .andExpect(jsonPath("$.events[?(@.name == 'Concierto Rock')].dates[0].venueName").value("Estadio Nacional"))
                .andExpect(jsonPath("$.events[?(@.name == 'Teatro Clásico')].dates[0].venueName").value("Teatro Abril"));
    }

    @Test
    void shouldFilterByCity() throws Exception {
        mockMvc.perform(get("/v1/catalog/events")
                .param("city", "Quetzaltenango")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events.length()").value(1))
                .andExpect(jsonPath("$.events[0].name").value("Teatro Clásico"));
    }

    @Test
    void shouldFilterByOrganizer() throws Exception {
        mockMvc.perform(get("/v1/catalog/events")
                .param("organizerId", organizerId.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.events.length()").value(2));
    }
}
