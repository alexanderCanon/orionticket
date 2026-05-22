package com.orionticket.ticketissuance.infrastructure;

import com.orionticket.ticketissuance.application.port.in.CancelTicketUseCase;
import com.orionticket.ticketissuance.application.port.in.InvalidateTicketUseCase;
import com.orionticket.ticketissuance.application.port.in.IssueTicketUseCase;
import com.orionticket.ticketissuance.application.port.in.TicketQueryUseCase;
import com.orionticket.ticketissuance.application.port.in.command.CancelTicketCommand;
import com.orionticket.ticketissuance.domain.model.DeliveryChannel;
import com.orionticket.ticketissuance.domain.model.Ticket;
import com.orionticket.ticketissuance.domain.model.TicketStatus;
import com.orionticket.ticketissuance.domain.model.TicketType;
import com.orionticket.ticketissuance.infrastructure.adapters.in.rest.GlobalExceptionHandler;
import com.orionticket.ticketissuance.infrastructure.adapters.in.rest.TicketController;
import com.orionticket.ticketissuance.infrastructure.adapters.in.rest.mapper.TicketRestMapper;
import com.orionticket.ticketissuance.infrastructure.config.SecurityConfig;
import com.orionticket.ticketissuance.infrastructure.security.AuthenticatedUserResolver;
import com.orionticket.ticketissuance.infrastructure.security.JwtAuthoritiesConverter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TicketController.class)
@Import({
        SecurityConfig.class,
        JwtAuthoritiesConverter.class,
        AuthenticatedUserResolver.class,
        TicketRestMapper.class,
        GlobalExceptionHandler.class
})
@TestPropertySource(properties = {
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://identity-service:8081/.well-known/jwks.json",
        "jwt.issuer=orionticket-identity"
})
class SecurityAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TicketQueryUseCase ticketQueryUseCase;

    @MockBean
    private IssueTicketUseCase issueTicketUseCase;

    @MockBean
    private CancelTicketUseCase cancelTicketUseCase;

    @MockBean
    private InvalidateTicketUseCase invalidateTicketUseCase;

    @Test
    void buyerTicketsWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/v1/buyers/" + UUID.randomUUID() + "/tickets"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void buyerCannotListAnotherBuyerTickets() throws Exception {
        UUID tokenBuyerId = UUID.randomUUID();
        UUID requestedBuyerId = UUID.randomUUID();

        mockMvc.perform(get("/v1/buyers/" + requestedBuyerId + "/tickets")
                        .with(jwt().jwt(jwt -> jwt
                                        .subject(tokenBuyerId.toString())
                                        .claim("role", "BUYER"))
                                .authorities(new SimpleGrantedAuthority("ROLE_BUYER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void buyerCanListOwnTickets() throws Exception {
        UUID buyerId = UUID.randomUUID();
        when(ticketQueryUseCase.listBuyerTickets(buyerId, 0, 20)).thenReturn(List.of());

        mockMvc.perform(get("/v1/buyers/" + buyerId + "/tickets")
                        .with(jwt().jwt(jwt -> jwt
                                        .subject(buyerId.toString())
                                        .claim("role", "BUYER"))
                                .authorities(new SimpleGrantedAuthority("ROLE_BUYER"))))
                .andExpect(status().isOk());

        verify(ticketQueryUseCase).listBuyerTickets(buyerId, 0, 20);
    }

    @Test
    void doorValidatorCanReadTicketForValidation() throws Exception {
        UUID ticketId = UUID.randomUUID();
        Ticket ticket = ticket(ticketId, UUID.randomUUID());
        when(ticketQueryUseCase.getTicket(ticketId)).thenReturn(ticket);

        mockMvc.perform(get("/v1/tickets/" + ticketId)
                        .with(jwt().jwt(jwt -> jwt
                                        .subject(UUID.randomUUID().toString())
                                        .claim("role", "DOOR_VALIDATOR"))
                                .authorities(new SimpleGrantedAuthority("ROLE_DOOR_VALIDATOR"))))
                .andExpect(status().isOk());
    }

    @Test
    void buyerCannotReadAnotherBuyerTicket() throws Exception {
        UUID ticketId = UUID.randomUUID();
        Ticket ticket = ticket(ticketId, UUID.randomUUID());
        when(ticketQueryUseCase.getTicket(ticketId)).thenReturn(ticket);

        mockMvc.perform(get("/v1/tickets/" + ticketId)
                        .with(jwt().jwt(jwt -> jwt
                                        .subject(UUID.randomUUID().toString())
                                        .claim("role", "BUYER"))
                                .authorities(new SimpleGrantedAuthority("ROLE_BUYER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void buyerCannotCancelTicket() throws Exception {
        mockMvc.perform(put("/v1/tickets/" + UUID.randomUUID() + "/cancel")
                        .with(jwt().jwt(jwt -> jwt
                                        .subject(UUID.randomUUID().toString())
                                        .claim("role", "BUYER"))
                                .authorities(new SimpleGrantedAuthority("ROLE_BUYER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void platformOperatorCanCancelTicket() throws Exception {
        UUID ticketId = UUID.randomUUID();
        Ticket canceledTicket = ticket(ticketId, UUID.randomUUID(), TicketStatus.CANCELED);
        when(cancelTicketUseCase.cancelTicket(eq(new CancelTicketCommand(ticketId)))).thenReturn(canceledTicket);

        mockMvc.perform(put("/v1/tickets/" + ticketId + "/cancel")
                        .with(jwt().jwt(jwt -> jwt
                                        .subject(UUID.randomUUID().toString())
                                        .claim("role", "PLATFORM_OPERATOR"))
                                .authorities(new SimpleGrantedAuthority("ROLE_PLATFORM_OPERATOR"))))
                .andExpect(status().isOk());
    }

    @Test
    void buyerCannotIssueTicketThroughRest() throws Exception {
        mockMvc.perform(post("/v1/tickets")
                        .with(jwt().jwt(jwt -> jwt
                                        .subject(UUID.randomUUID().toString())
                                        .claim("role", "BUYER"))
                                .authorities(new SimpleGrantedAuthority("ROLE_BUYER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(issueTicketRequest()))
                .andExpect(status().isForbidden());
    }

    private static String issueTicketRequest() {
        return """
                {
                  "orderId": "%s",
                  "buyerId": "%s",
                  "eventId": "%s",
                  "dateId": "%s",
                  "seatId": "%s",
                  "type": "MAPPED",
                  "holderName": "Test Buyer",
                  "qrCode": "qr-token",
                  "qrExpiresAt": "%s",
                  "accessPolicy": "MAIN_GATE",
                  "deliveryChannels": ["EMAIL"]
                }
                """.formatted(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.now().plusSeconds(120));
    }

    private static Ticket ticket(UUID ticketId, UUID buyerId) {
        return ticket(ticketId, buyerId, TicketStatus.ISSUED);
    }

    private static Ticket ticket(UUID ticketId, UUID buyerId, TicketStatus status) {
        return new Ticket(
                ticketId,
                UUID.randomUUID(),
                buyerId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                TicketType.MAPPED,
                "Test Buyer",
                "qr-token",
                Instant.now().plusSeconds(120),
                "MAIN_GATE",
                status,
                null,
                Instant.now(),
                Set.of(DeliveryChannel.EMAIL));
    }
}
