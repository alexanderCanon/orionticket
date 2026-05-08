package com.orionticket.events.infrastructure;

import com.orionticket.events.infrastructure.adapters.out.persistence.entity.EventJpaEntity;
import com.orionticket.events.infrastructure.adapters.out.persistence.repository.SpringDataEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class EventManagementIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("event_db")
            .withUsername("orion")
            .withPassword("secret");

    @Container
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3.13-management");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
        
        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
    }

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
                    "description": "Prueba desde Testcontainers"
                }
                """;

        mockMvc.perform(post("/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.name").value("Concierto Integracion"));
    }

    @Test
    void shouldAddDateToEvent() throws Exception {
        // Preparar evento en BD
        UUID eventId = UUID.randomUUID();
        EventJpaEntity event = new EventJpaEntity();
        event.setEventId(eventId);
        event.setOrganizerId(UUID.fromString("00000000-0000-0000-0000-000000000002"));
        event.setName("Evento Base");
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
}
