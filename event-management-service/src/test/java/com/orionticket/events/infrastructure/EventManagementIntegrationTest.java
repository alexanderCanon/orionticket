package com.orionticket.events.infrastructure;

import com.orionticket.events.infrastructure.adapters.out.persistence.entity.EventJpaEntity;
import com.orionticket.events.infrastructure.adapters.out.persistence.repository.SpringDataEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EventManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SpringDataEventRepository eventRepository;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
    }

    @Test
    void shouldCreateEvent() throws Exception {
        String requestBody = """
                {
                    "name": "Concierto Integracion",
                    "description": "Prueba desde Testcontainers",
                    "category": "MUSIC"
                }
                """;

        mockMvc.perform(post("/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.name").value("Concierto Integracion"))
                .andExpect(jsonPath("$.category").value("MUSIC"));
    }

    @Test
    void shouldAddDateToEvent() throws Exception {
        // Preparar evento en BD
        UUID eventId = UUID.randomUUID();
        EventJpaEntity event = new EventJpaEntity();
        event.setEventId(eventId);
        event.setOrganizerId(UUID.fromString("00000000-0000-0000-0000-000000000002"));
        event.setName("Evento Base");
        event.setCategory("MUSIC");
        event.setStatus("DRAFT");
        event.setCreatedAt(ZonedDateTime.now());
        eventRepository.save(event);

        String requestBody = """
                {
                    "scheduledAt": "2026-12-31T20:00:00Z",
                    "venueId": "11111111-1111-1111-1111-111111111111",
                    "capacity": 500
                }
                """;

        mockMvc.perform(post("/v1/events/" + eventId + "/dates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.capacity").value(500));
    }

    @Test
    void shouldCreateVenue() throws Exception {
        String requestBody = """
                {
                    "name": "Estadio Nacional",
                    "address": "Zona 5, Guatemala",
                    "capacity": 25000
                }
                """;

        mockMvc.perform(post("/v1/venues")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Estadio Nacional"))
                .andExpect(jsonPath("$.capacity").value(25000));
    }

    @Test
    void shouldSubmitEventForReview() throws Exception {
        // Preparar evento con fecha en BD
        UUID eventId = UUID.randomUUID();
        EventJpaEntity event = new EventJpaEntity();
        event.setEventId(eventId);
        event.setOrganizerId(UUID.fromString("00000000-0000-0000-0000-000000000002"));
        event.setName("Evento para Review");
        event.setCategory("SPORTS");
        event.setStatus("DRAFT");
        event.setCreatedAt(ZonedDateTime.now());

        com.orionticket.events.infrastructure.adapters.out.persistence.entity.EventDateJpaEntity date = 
            new com.orionticket.events.infrastructure.adapters.out.persistence.entity.EventDateJpaEntity();
        date.setDateId(UUID.randomUUID());
        date.setEvent(event);
        date.setScheduledAt(ZonedDateTime.now().plusDays(5));
        date.setVenueId(UUID.randomUUID());
        date.setCapacity(100);
        date.setCreatedAt(ZonedDateTime.now());
        
        event.getDates().add(date);
        eventRepository.save(event);

        mockMvc.perform(post("/v1/events/" + eventId + "/submit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UNDER_REVIEW"));
    }

    @Test
    void shouldApproveEvent() throws Exception {
        UUID eventId = UUID.randomUUID();
        EventJpaEntity event = new EventJpaEntity();
        event.setEventId(eventId);
        event.setOrganizerId(UUID.fromString("00000000-0000-0000-0000-000000000002"));
        event.setName("Evento para Aprobar");
        event.setCategory("MUSIC");
        event.setStatus("UNDER_REVIEW");
        event.setCreatedAt(ZonedDateTime.now());
        eventRepository.save(event);

        mockMvc.perform(post("/v1/events/" + eventId + "/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RELEASED"));
    }

    @Test
    void shouldRejectEvent() throws Exception {
        UUID eventId = UUID.randomUUID();
        EventJpaEntity event = new EventJpaEntity();
        event.setEventId(eventId);
        event.setOrganizerId(UUID.fromString("00000000-0000-0000-0000-000000000002"));
        event.setName("Evento para Rechazar");
        event.setCategory("MUSIC");
        event.setStatus("UNDER_REVIEW");
        event.setCreatedAt(ZonedDateTime.now());
        eventRepository.save(event);

        String requestBody = """
                {
                    "reason": "Faltan detalles de seguridad"
                }
                """;

        mockMvc.perform(post("/v1/events/" + eventId + "/reject")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.rejectionReason").value("Faltan detalles de seguridad"));
    }

    @Test
    void shouldCancelEvent() throws Exception {
        UUID eventId = UUID.randomUUID();
        EventJpaEntity event = new EventJpaEntity();
        event.setEventId(eventId);
        event.setOrganizerId(UUID.fromString("00000000-0000-0000-0000-000000000002"));
        event.setName("Evento para Cancelar");
        event.setCategory("MUSIC");
        event.setStatus("RELEASED");
        event.setCreatedAt(ZonedDateTime.now());
        eventRepository.save(event);

        String requestBody = """
                {
                    "reason": "Fuerza mayor — condiciones meteorológicas adversas"
                }
                """;

        mockMvc.perform(post("/v1/events/" + eventId + "/cancel")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }

    @Test
    void shouldReturn400WhenCancelEventWithoutReason() throws Exception {
        UUID eventId = UUID.randomUUID();
        EventJpaEntity event = new EventJpaEntity();
        event.setEventId(eventId);
        event.setOrganizerId(UUID.fromString("00000000-0000-0000-0000-000000000002"));
        event.setName("Evento para Cancelar");
        event.setCategory("MUSIC");
        event.setStatus("RELEASED");
        event.setCreatedAt(ZonedDateTime.now());
        eventRepository.save(event);

        mockMvc.perform(post("/v1/events/" + eventId + "/cancel")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
