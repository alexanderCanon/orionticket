package com.orionticket.seating.shared.infrastructure;

import com.orionticket.seating.batch.application.port.in.BatchManagementUseCase;
import com.orionticket.seating.batch.infrastructure.adapters.in.rest.BatchController;
import com.orionticket.seating.reservation.application.port.in.ReservationUseCase;
import com.orionticket.seating.reservation.domain.model.Reservation;
import com.orionticket.seating.reservation.infrastructure.adapters.in.rest.ReservationController;
import com.orionticket.seating.seat.application.port.in.SeatAvailabilityUseCase;
import com.orionticket.seating.seat.application.port.in.SeatingMapUseCase;
import com.orionticket.seating.seat.infrastructure.adapters.in.rest.SeatAvailabilityController;
import com.orionticket.seating.seat.infrastructure.adapters.in.rest.SeatingMapController;
import com.orionticket.seating.shared.infrastructure.config.SecurityConfig;
import com.orionticket.seating.shared.infrastructure.security.AuthenticatedUserResolver;
import com.orionticket.seating.shared.infrastructure.security.JwtAuthoritiesConverter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        SeatAvailabilityController.class,
        SeatingMapController.class,
        BatchController.class,
        ReservationController.class
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
    private SeatAvailabilityUseCase seatAvailabilityUseCase;

    @MockBean
    private SeatingMapUseCase seatingMapUseCase;

    @MockBean
    private BatchManagementUseCase batchManagementUseCase;

    @MockBean
    private ReservationUseCase reservationUseCase;

    @Test
    void seatAvailabilityEndpointRemainsPublic() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID dateId = UUID.randomUUID();
        when(seatAvailabilityUseCase.getAvailableSeats(eventId, dateId, null, null))
                .thenReturn(List.of());

        mockMvc.perform(get("/v1/events/" + eventId + "/dates/" + dateId + "/seats"))
                .andExpect(status().isOk());
    }

    @Test
    void createReservationWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservationRequest(UUID.randomUUID(), UUID.randomUUID())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void buyerCreatesReservationWithBuyerIdFromToken() throws Exception {
        UUID buyerId = UUID.randomUUID();
        UUID forgedBuyerId = UUID.randomUUID();
        UUID seatId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID dateId = UUID.randomUUID();
        UUID batchId = UUID.randomUUID();
        Reservation reservation = Reservation.builder()
                .reservationId(UUID.randomUUID())
                .seatId(seatId)
                .buyerId(buyerId)
                .eventId(eventId)
                .dateId(dateId)
                .batchId(batchId)
                .expiresAt(ZonedDateTime.now().plusMinutes(10))
                .status("ACTIVE")
                .build();
        when(reservationUseCase.createReservation(seatId, buyerId, eventId, dateId, batchId))
                .thenReturn(reservation);

        mockMvc.perform(post("/v1/reservations")
                        .with(jwt().jwt(jwt -> jwt
                                        .subject(buyerId.toString())
                                        .claim("role", "BUYER"))
                                .authorities(new SimpleGrantedAuthority("ROLE_BUYER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "seatId": "%s",
                                  "buyerId": "%s",
                                  "eventId": "%s",
                                  "dateId": "%s",
                                  "batchId": "%s"
                                }
                                """.formatted(seatId, forgedBuyerId, eventId, dateId, batchId)))
                .andExpect(status().isCreated());

        verify(reservationUseCase).createReservation(seatId, buyerId, eventId, dateId, batchId);
    }

    @Test
    void buyerCannotReleaseReservation() throws Exception {
        mockMvc.perform(delete("/v1/reservations/" + UUID.randomUUID())
                        .with(jwt().jwt(jwt -> jwt.claim("role", "BUYER"))
                                .authorities(new SimpleGrantedAuthority("ROLE_BUYER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void platformOperatorCanReleaseReservation() throws Exception {
        UUID reservationId = UUID.randomUUID();
        Reservation reservation = Reservation.builder()
                .reservationId(reservationId)
                .seatId(UUID.randomUUID())
                .buyerId(UUID.randomUUID())
                .eventId(UUID.randomUUID())
                .dateId(UUID.randomUUID())
                .batchId(UUID.randomUUID())
                .expiresAt(ZonedDateTime.now().plusMinutes(10))
                .status("RELEASED")
                .build();
        when(reservationUseCase.releaseReservation(reservationId)).thenReturn(reservation);

        mockMvc.perform(delete("/v1/reservations/" + reservationId)
                        .with(jwt().jwt(jwt -> jwt.claim("role", "PLATFORM_OPERATOR"))
                                .authorities(new SimpleGrantedAuthority("ROLE_PLATFORM_OPERATOR"))))
                .andExpect(status().isOk());
    }

    @Test
    void createBatchRequiresOrganizerRole() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID dateId = UUID.randomUUID();

        mockMvc.perform(post("/v1/events/" + eventId + "/dates/" + dateId + "/batches")
                        .with(jwt().jwt(jwt -> jwt.claim("role", "BUYER"))
                                .authorities(new SimpleGrantedAuthority("ROLE_BUYER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Early Bird",
                                  "price": 100.00,
                                  "currency": "GTQ",
                                  "capacity": 100,
                                  "scheduledStartAt": "2026-12-31T20:00:00Z"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    private static String reservationRequest(UUID seatId, UUID buyerId) {
        return """
                {
                  "seatId": "%s",
                  "buyerId": "%s",
                  "eventId": "%s",
                  "dateId": "%s",
                  "batchId": "%s"
                }
                """.formatted(seatId, buyerId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    }
}
