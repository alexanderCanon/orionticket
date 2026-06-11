package com.orionticket.ticketissuance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orionticket.ticketissuance.domain.model.DeliveryChannel;
import com.orionticket.ticketissuance.domain.model.TicketType;
import com.orionticket.ticketissuance.infrastructure.adapters.in.rest.dto.IssueTicketRequest;
import com.orionticket.ticketissuance.infrastructure.adapters.out.persistence.repository.JpaTicketRepository;
import com.orionticket.ticketissuance.shared.infrastructure.config.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = TicketIssuanceApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
public class TicketIssuanceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JpaTicketRepository ticketRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Test
    void testEndToEndTicketLifecycle() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID dateId = UUID.randomUUID();
        UUID seatId = UUID.randomUUID();

        // Declare exchange and test queue to capture notification event
        TopicExchange exchange = new TopicExchange("notification-events", true, false);
        amqpAdmin.declareExchange(exchange);

        String testQueueName = "test.ticket.issued.queue-" + UUID.randomUUID();
        Queue testQueue = new Queue(testQueueName, false, false, true);
        amqpAdmin.declareQueue(testQueue);
        Binding binding = BindingBuilder.bind(testQueue)
                .to(exchange)
                .with("#");
        amqpAdmin.declareBinding(binding);

        UUID ticketId;

        try {
            // 1. Issue Ticket (Requires SUPER_ADMIN role)
            IssueTicketRequest issueRequest = new IssueTicketRequest(
                    orderId,
                    buyerId,
                    eventId,
                    dateId,
                    seatId,
                    TicketType.MAPPED,
                    "Alex Avers",
                    "qr-code-secret-hash",
                    Instant.now().plusSeconds(3600),
                    "ONE_ENTRY_ONLY",
                    Set.of(DeliveryChannel.EMAIL)
            );

            String ticketResponseJson = mockMvc.perform(post("/v1/tickets")
                            .with(jwt().jwt(jwt -> jwt.claim("role", "SUPER_ADMIN"))
                                    .authorities(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(issueRequest)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            Map<?, ?> ticketResponse = objectMapper.readValue(ticketResponseJson, Map.class);
            String ticketIdStr = (String) ticketResponse.get("ticketId");
            ticketId = UUID.fromString(ticketIdStr);

            // Verify ticket exists in Database
            var ticketOpt = ticketRepository.findById(ticketId);
            assertThat(ticketOpt).isPresent();
            assertThat(ticketOpt.get().getStatus()).isEqualTo("ISSUED");
            assertThat(ticketOpt.get().getHolderName()).isEqualTo("Alex Avers");

            // Verify TicketIssued event published to RabbitMQ
            Object messageObj = rabbitTemplate.receiveAndConvert(testQueueName, 5000);
            assertThat(messageObj).isNotNull();

            @SuppressWarnings("unchecked")
            Map<String, Object> receivedMessage = objectMapper.readValue((String) messageObj, Map.class);
            assertThat(receivedMessage.get("eventType")).isEqualTo("TicketIssued");
            assertThat(receivedMessage.get("recipientId")).isEqualTo(buyerId.toString());

            // 2. Query Ticket details (Requires BUYER role with self validation)
            mockMvc.perform(get("/v1/tickets/" + ticketId)
                            .with(jwt().jwt(jwt -> jwt.subject(buyerId.toString()).claim("role", "BUYER"))
                                    .authorities(new SimpleGrantedAuthority("ROLE_BUYER"))))
                    .andExpect(status().isOk());

            // 3. Invalidate Ticket (Requires PLATFORM_OPERATOR role)
            mockMvc.perform(put("/v1/tickets/" + ticketId + "/invalidate")
                            .with(jwt().jwt(jwt -> jwt.claim("role", "PLATFORM_OPERATOR"))
                                    .authorities(new SimpleGrantedAuthority("ROLE_PLATFORM_OPERATOR"))))
                    .andExpect(status().isOk());

            // Verify status updated in DB
            var invalidatedTicket = ticketRepository.findById(ticketId).orElseThrow();
            assertThat(invalidatedTicket.getStatus()).isEqualTo("INVALIDATED");

        } finally {
            amqpAdmin.deleteQueue(testQueueName);
        }
    }
}
