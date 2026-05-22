package com.orionticket.notifications.infrastructure.adapters.in.rest;

import com.orionticket.notifications.application.port.in.NotificationQueryUseCase;
import com.orionticket.notifications.application.port.in.RetryNotificationUseCase;
import com.orionticket.notifications.application.port.in.model.NotificationPage;
import com.orionticket.notifications.domain.model.Notification;
import com.orionticket.notifications.domain.model.NotificationChannel;
import com.orionticket.notifications.domain.model.NotificationStatus;
import com.orionticket.notifications.infrastructure.adapters.in.rest.mapper.NotificationRestMapper;
import com.orionticket.notifications.infrastructure.config.JwtAuthoritiesConverter;
import com.orionticket.notifications.infrastructure.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NotificationController.class)
@Import({
        SecurityConfig.class,
        JwtAuthoritiesConverter.class,
        NotificationRestMapper.class
})
@TestPropertySource(properties = {
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://identity-service:8081/.well-known/jwks.json",
        "jwt.issuer=orionticket-identity"
})
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationQueryUseCase notificationQueryUseCase;

    @MockBean
    private RetryNotificationUseCase retryNotificationUseCase;

    @Test
    void listNotificationsReturnsDeliveryLog() throws Exception {
        Notification notification = notification(NotificationStatus.FAILED);
        when(notificationQueryUseCase.findNotifications(
                eq(null),
                eq(NotificationStatus.FAILED),
                eq(NotificationChannel.EMAIL),
                eq(null),
                eq(0),
                eq(20)
        )).thenReturn(new NotificationPage(List.of(notification), 0, 1));

        mockMvc.perform(get("/v1/notifications")
                        .param("status", "FAILED")
                        .param("channel", "EMAIL")
                        .with(supportJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notifications[0].notificationId").value(notification.notificationId().toString()))
                .andExpect(jsonPath("$.notifications[0].status").value("FAILED"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void getNotificationReturnsPayloadObject() throws Exception {
        Notification notification = notification(NotificationStatus.DELIVERED);
        when(notificationQueryUseCase.findNotification(notification.notificationId()))
                .thenReturn(java.util.Optional.of(notification));

        mockMvc.perform(get("/v1/notifications/{notificationId}", notification.notificationId())
                        .with(supportJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationId").value(notification.notificationId().toString()))
                .andExpect(jsonPath("$.payload.ticketId").value("ticket-1"));
    }

    @Test
    void retryFailedReturnsOperationalSummary() throws Exception {
        when(retryNotificationUseCase.retryFailedNotifications())
                .thenReturn(List.of(
                        notification(NotificationStatus.DELIVERED),
                        notification(NotificationStatus.FAILED)
                ));

        mockMvc.perform(post("/v1/notifications/retry-failed")
                        .with(platformOperatorJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processed").value(2))
                .andExpect(jsonPath("$.delivered").value(1))
                .andExpect(jsonPath("$.failed").value(1));
    }

    private Notification notification(NotificationStatus status) {
        return new Notification(
                UUID.randomUUID(),
                UUID.randomUUID(),
                NotificationChannel.EMAIL,
                "ticket-issued",
                "{\"ticketId\":\"ticket-1\"}",
                status,
                1,
                "TICKET_ISSUED",
                Instant.parse("2026-05-22T00:00:00Z"),
                "provider-message-id",
                null
        );
    }

    private RequestPostProcessor supportJwt() {
        return jwt().jwt(jwt -> jwt
                        .subject(UUID.randomUUID().toString())
                        .claim("role", "SUPPORT"))
                .authorities(new SimpleGrantedAuthority("ROLE_SUPPORT"));
    }

    private RequestPostProcessor platformOperatorJwt() {
        return jwt().jwt(jwt -> jwt
                        .subject(UUID.randomUUID().toString())
                        .claim("role", "PLATFORM_OPERATOR"))
                .authorities(new SimpleGrantedAuthority("ROLE_PLATFORM_OPERATOR"));
    }
}
