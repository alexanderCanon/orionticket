package com.orionticket.seating;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orionticket.seating.batch.infrastructure.adapters.out.persistence.repository.SpringDataBatchRepository;
import com.orionticket.seating.reservation.infrastructure.adapters.out.persistence.repository.SpringDataReservationRepository;
import com.orionticket.seating.seat.infrastructure.adapters.out.persistence.entity.SeatJpaEntity;
import com.orionticket.seating.seat.infrastructure.adapters.out.persistence.repository.SpringDataSeatRepository;
import com.orionticket.seating.shared.infrastructure.config.RabbitMqConfig;
import com.orionticket.seating.shared.infrastructure.config.TestcontainersConfiguration;
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

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = SeatingInventoryApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
public class SeatingInventoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SpringDataBatchRepository batchRepository;

    @Autowired
    private SpringDataSeatRepository seatRepository;

    @Autowired
    private SpringDataReservationRepository reservationRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Test
    void testEndToEndSeatingAndReservationFlow() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID dateId = UUID.randomUUID();

        // 1. Create a Batch (Requires ORGANIZER role)
        Map<String, Object> createBatchBody = Map.of(
                "name", "Early Bird",
                "price", new BigDecimal("150.00"),
                "currency", "GTQ",
                "capacity", 5,
                "scheduledStartAt", ZonedDateTime.now().minusHours(1).toString()
        );

        String batchResponseJson = mockMvc.perform(post("/v1/events/" + eventId + "/dates/" + dateId + "/batches")
                        .with(jwt().jwt(jwt -> jwt.claim("role", "ORGANIZER"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ORGANIZER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBatchBody)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Map<?, ?> batchResponse = objectMapper.readValue(batchResponseJson, Map.class);
        String batchIdStr = (String) batchResponse.get("batchId");
        UUID batchId = UUID.fromString(batchIdStr);

        // Verify batch created in DB
        assertThat(batchRepository.existsById(batchId)).isTrue();

        // 2. Configure Seating Map (Requires ORGANIZER role)
        Map<String, Object> configureSeatingMapBody = Map.of(
                "batchId", batchId,
                "generalAdmission", Map.of(
                        "capacity", 5,
                        "accessPolicy", "ONE_ENTRY_ONLY"
                )
        );

        mockMvc.perform(post("/v1/events/" + eventId + "/dates/" + dateId + "/seating-map")
                        .with(jwt().jwt(jwt -> jwt.claim("role", "ORGANIZER"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ORGANIZER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(configureSeatingMapBody)))
                .andExpect(status().isCreated());

        // Find configured seats
        List<SeatJpaEntity> seats = seatRepository.findByEventIdAndDateId(eventId, dateId);
        assertThat(seats).hasSize(5);
        UUID seatId = seats.get(0).getSeatId();

        // Declare a test queue to verify ReservationCreated message
        String testQueueName = "test.reservation.created.queue-" + UUID.randomUUID();
        Queue testQueue = new Queue(testQueueName, false, false, true);
        amqpAdmin.declareQueue(testQueue);
        Binding binding = BindingBuilder.bind(testQueue)
                .to(new TopicExchange(RabbitMqConfig.EXCHANGE_NAME))
                .with(RabbitMqConfig.RESERVATION_CREATED_KEY);
        amqpAdmin.declareBinding(binding);

        UUID buyerId = UUID.randomUUID();
        UUID reservationId;

        try {
            // 3. Create Reservation (Requires BUYER role)
            Map<String, Object> createReservationBody = Map.of(
                    "seatId", seatId,
                    "buyerId", buyerId,
                    "eventId", eventId,
                    "dateId", dateId,
                    "batchId", batchId
            );

            String reservationResponseJson = mockMvc.perform(post("/v1/reservations")
                            .with(jwt().jwt(jwt -> jwt.subject(buyerId.toString()).claim("role", "BUYER"))
                                    .authorities(new SimpleGrantedAuthority("ROLE_BUYER")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createReservationBody)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            Map<?, ?> reservationResponse = objectMapper.readValue(reservationResponseJson, Map.class);
            String reservationIdStr = (String) reservationResponse.get("reservationId");
            reservationId = UUID.fromString(reservationIdStr);

            // Verify Reservation saved in DB and Seat status changed to RESERVED
            assertThat(reservationRepository.existsById(reservationId)).isTrue();
            SeatJpaEntity reservedSeat = seatRepository.findById(seatId).orElseThrow();
            assertThat(reservedSeat.getStatus()).isEqualTo("RESERVED");

            // Verify Batch sold counter incremented to 1
            var updatedBatch = batchRepository.findById(batchId).orElseThrow();
            assertThat(updatedBatch.getSold()).isEqualTo(1);

            // Verify ReservationCreated event published to RabbitMQ
            Object messageObj = rabbitTemplate.receiveAndConvert(testQueueName, 5000);
            assertThat(messageObj).isNotNull();

            @SuppressWarnings("unchecked")
            Map<String, Object> receivedMessage = (Map<String, Object>) messageObj;
            assertThat(receivedMessage.get("eventType")).isEqualTo("ReservationCreated");

            @SuppressWarnings("unchecked")
            Map<String, Object> receivedPayload = (Map<String, Object>) receivedMessage.get("payload");
            assertThat(receivedPayload.get("reservationId")).isEqualTo(reservationId.toString());
            assertThat(receivedPayload.get("seatId")).isEqualTo(seatId.toString());
            assertThat(receivedPayload.get("buyerId")).isEqualTo(buyerId.toString());

            // 4. Concurrency test: try reserving the same seat again -> should return 409
            mockMvc.perform(post("/v1/reservations")
                            .with(jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString()).claim("role", "BUYER"))
                                    .authorities(new SimpleGrantedAuthority("ROLE_BUYER")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createReservationBody)))
                    .andExpect(status().isConflict());

        } finally {
            amqpAdmin.deleteQueue(testQueueName);
        }

        // Declare a test queue to verify ReservationReleased message
        String releaseTestQueueName = "test.reservation.released.queue-" + UUID.randomUUID();
        Queue releaseTestQueue = new Queue(releaseTestQueueName, false, false, true);
        amqpAdmin.declareQueue(releaseTestQueue);
        Binding releaseBinding = BindingBuilder.bind(releaseTestQueue)
                .to(new TopicExchange(RabbitMqConfig.EXCHANGE_NAME))
                .with(RabbitMqConfig.RESERVATION_RELEASED_KEY);
        amqpAdmin.declareBinding(releaseBinding);

        try {
            // 5. Simulate PaymentFailed event -> should release reservation
            Map<String, Object> paymentFailedPayload = Map.of(
                    "paymentId", UUID.randomUUID().toString(),
                    "orderId", UUID.randomUUID().toString(),
                    "reservationId", reservationId.toString()
            );
            Map<String, Object> paymentFailedEnvelope = Map.of(
                    "eventType", "PaymentFailed",
                    "eventId", UUID.randomUUID().toString(),
                    "occurredAt", ZonedDateTime.now().toString(),
                    "payload", paymentFailedPayload
            );

            rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE_NAME, RabbitMqConfig.PAYMENT_FAILED_KEY, paymentFailedEnvelope);

            // Wait for consumer to process message
            boolean released = false;
            for (int i = 0; i < 50; i++) {
                var reservation = reservationRepository.findById(reservationId).orElseThrow();
                if ("RELEASED".equals(reservation.getStatus())) {
                    released = true;
                    break;
                }
                Thread.sleep(100);
            }
            assertThat(released).isTrue();

            // Verify Seat is AVAILABLE again
            SeatJpaEntity releasedSeat = seatRepository.findById(seatId).orElseThrow();
            assertThat(releasedSeat.getStatus()).isEqualTo("AVAILABLE");

            // Verify Batch sold counter decremented back to 0
            var finalBatch = batchRepository.findById(batchId).orElseThrow();
            assertThat(finalBatch.getSold()).isEqualTo(0);

            // Verify ReservationReleased event published
            Object releaseMessageObj = rabbitTemplate.receiveAndConvert(releaseTestQueueName, 5000);
            assertThat(releaseMessageObj).isNotNull();

            @SuppressWarnings("unchecked")
            Map<String, Object> receivedReleaseMessage = (Map<String, Object>) releaseMessageObj;
            assertThat(receivedReleaseMessage.get("eventType")).isEqualTo("ReservationReleased");

        } finally {
            amqpAdmin.deleteQueue(releaseTestQueueName);
        }
    }
}
