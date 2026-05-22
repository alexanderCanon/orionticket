package com.orionticket.notifications.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orionticket.notifications.infrastructure.adapters.out.persistence.entity.NotificationEntity;
import com.orionticket.notifications.infrastructure.adapters.out.persistence.repository.JpaNotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.POST;

@SpringBootTest(properties = {
    "spring.flyway.enabled=true",
    "spring.flyway.schemas=notifications",
    "spring.flyway.create-schemas=true",
    "spring.jpa.properties.hibernate.default_schema=notifications",
    "spring.jpa.hibernate.ddl-auto=validate",
    "spring.rabbitmq.listener.simple.auto-startup=true",
    "spring.datasource.driver-class-name=org.postgresql.Driver",
    "notifications.email.enabled=true",
    "notifications.email.api-key=test-api-key",
    "notifications.email.from-email=tickets@orionticket.local",
    "notifications.email.from-name=OrionTicket"
})
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class NotificationIntegrationTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private JpaNotificationRepository notificationRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
        notificationRepository.deleteAll();
    }

    @Test
    void shouldProcessNotificationEventAndSendEmailViaResend() throws Exception {
        // Arrange
        UUID recipientId = UUID.randomUUID();
        String eventType = "TICKET_ISSUED";
        
        Map<String, Object> payloadMap = Map.of(
                "recipientEmail", "buyer@example.com",
                "recipientName", "Alex Avers",
                "eventName", "Festival Rock 2026",
                "eventDate", "2026-11-15T20:00:00Z",
                "venueName", "Estadio Central",
                "seatCode", "Zona VIP - Fila A - 12",
                "orderId", "ORD-998877",
                "ticketUrl", "https://orionticket.local/tickets/t-1122"
        );

        Map<String, Object> eventMsg = Map.of(
                "recipientId", recipientId.toString(),
                "channel", "EMAIL",
                "templateId", "ticket-issued",
                "payload", payloadMap,
                "eventType", eventType
        );

        String messageJson = objectMapper.writeValueAsString(eventMsg);

        // Expect Resend POST request
        mockServer.expect(requestTo("https://api.resend.com/emails"))
                .andExpect(method(POST))
                .andExpect(header("Authorization", "Bearer test-api-key"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withSuccess("{\"id\": \"re_123456789\"}", MediaType.APPLICATION_JSON));

        // Act
        rabbitTemplate.convertAndSend("notification-events", messageJson);

        // Assert: wait for the asynchronous listener to process and save
        NotificationEntity savedNotification = null;
        for (int i = 0; i < 50; i++) {
            List<NotificationEntity> notifications = notificationRepository.findAll();
            if (!notifications.isEmpty()) {
                NotificationEntity notif = notifications.get(0);
                if ("DELIVERED".equals(notif.getStatus())) {
                    savedNotification = notif;
                    break;
                }
            }
            TimeUnit.MILLISECONDS.sleep(100);
        }

        assertThat(savedNotification).isNotNull();
        assertThat(savedNotification.getRecipientId()).isEqualTo(recipientId);
        assertThat(savedNotification.getChannel()).isEqualTo("EMAIL");
        assertThat(savedNotification.getTemplateId()).isEqualTo("ticket-issued");
        assertThat(savedNotification.getStatus()).isEqualTo("DELIVERED");
        assertThat(savedNotification.getProviderMessageId()).isEqualTo("re_123456789");
        assertThat(savedNotification.getFailureReason()).isNull();
        assertThat(savedNotification.getTriggeredBy()).isEqualTo(eventType);

        mockServer.verify();
    }
}
