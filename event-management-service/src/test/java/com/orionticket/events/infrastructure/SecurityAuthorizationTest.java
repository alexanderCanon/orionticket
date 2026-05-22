package com.orionticket.events.infrastructure;

import com.orionticket.events.application.port.in.EventManagementUseCase;
import com.orionticket.events.application.port.in.GetEventCatalogUseCase;
import com.orionticket.events.application.port.in.VenueManagementUseCase;
import com.orionticket.events.domain.model.Event;
import com.orionticket.events.infrastructure.adapters.in.rest.EventCatalogController;
import com.orionticket.events.infrastructure.adapters.in.rest.EventManagementController;
import com.orionticket.events.infrastructure.adapters.in.rest.VenueManagementController;
import com.orionticket.events.infrastructure.adapters.out.security.AuthenticatedUserResolver;
import com.orionticket.events.infrastructure.adapters.out.security.JwtAuthoritiesConverter;
import com.orionticket.events.infrastructure.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        EventCatalogController.class,
        EventManagementController.class,
        VenueManagementController.class
})
@Import({
        SecurityConfig.class,
        JwtAuthoritiesConverter.class,
        AuthenticatedUserResolver.class
})
class SecurityAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventManagementUseCase eventManagementUseCase;

    @MockBean
    private VenueManagementUseCase venueManagementUseCase;

    @MockBean
    private GetEventCatalogUseCase getEventCatalogUseCase;

    @Test
    void catalogEndpointRemainsPublic() throws Exception {
        when(getEventCatalogUseCase.getCatalog(any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/v1/catalog/events"))
                .andExpect(status().isOk());
    }

    @Test
    void createEventWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Festival",
                                  "description": "Outdoor festival",
                                  "category": "MUSIC"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void organizerCreatesEventWithOrganizerIdFromToken() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID organizerId = UUID.randomUUID();
        Event event = Event.builder()
                .eventId(UUID.randomUUID())
                .organizerId(organizerId)
                .name("Festival")
                .description("Outdoor festival")
                .category("MUSIC")
                .status("DRAFT")
                .createdAt(ZonedDateTime.now())
                .build();
        when(eventManagementUseCase.createEvent(eq(organizerId), eq("Festival"), eq("Outdoor festival"), eq("MUSIC")))
                .thenReturn(event);

        mockMvc.perform(post("/v1/events")
                        .with(jwt().jwt(jwt -> jwt
                                        .subject(userId.toString())
                                        .claim("role", "ORGANIZER")
                                        .claim("organizerId", organizerId.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_ORGANIZER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Festival",
                                  "description": "Outdoor festival",
                                  "category": "MUSIC"
                                }
                                """))
                .andExpect(status().isCreated());

        verify(eventManagementUseCase).createEvent(organizerId, "Festival", "Outdoor festival", "MUSIC");
    }

    @Test
    void buyerCannotApproveEvent() throws Exception {
        mockMvc.perform(post("/v1/events/" + UUID.randomUUID() + "/approve")
                        .with(jwt().jwt(jwt -> jwt.claim("role", "BUYER"))
                                .authorities(new SimpleGrantedAuthority("ROLE_BUYER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void platformOperatorApprovesEventWithOperatorIdFromToken() throws Exception {
        UUID operatorId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        Event event = Event.builder()
                .eventId(eventId)
                .organizerId(UUID.randomUUID())
                .name("Festival")
                .category("MUSIC")
                .status("RELEASED")
                .createdAt(ZonedDateTime.now())
                .build();
        when(eventManagementUseCase.approveEvent(eventId, operatorId)).thenReturn(event);

        mockMvc.perform(post("/v1/events/" + eventId + "/approve")
                        .with(jwt().jwt(jwt -> jwt
                                        .subject(operatorId.toString())
                                        .claim("role", "PLATFORM_OPERATOR"))
                                .authorities(new SimpleGrantedAuthority("ROLE_PLATFORM_OPERATOR"))))
                .andExpect(status().isOk());

        verify(eventManagementUseCase).approveEvent(eventId, operatorId);
    }
}
