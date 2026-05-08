package com.orionticket.acesscontrol.infrastructure.adapters.in.rest;

import com.orionticket.acesscontrol.base.IntegrationTestBase;
import com.orionticket.acesscontrol.infrastructure.adapters.in.rest.dto.request.SyncRequestDto;
import com.orionticket.acesscontrol.infrastructure.adapters.in.rest.dto.request.ValidationRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ValidationControllerTest extends IntegrationTestBase {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /v1/validations - returns mock validation response")
    @WithMockUser(roles = "DOOR_VALIDATOR")
    void validateTicket_shouldReturnMockResponse() throws Exception {
        ValidationRequestDto request = new ValidationRequestDto(
                UUID.randomUUID(),
                "device-001",
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        mockMvc.perform(post("/v1/validations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.validationId").exists())
                .andExpect(jsonPath("$.ticketId").exists())
                .andExpect(jsonPath("$.result").value("SUCCEEDED"))
                .andExpect(jsonPath("$.isOffline").value(false))
                .andExpect(jsonPath("$.attemptedAt").exists());
    }

    @Test
    @DisplayName("POST /v1/validations/sync - returns mock sync response")
    @WithMockUser(roles = "DOOR_VALIDATOR")
    void syncValidations_shouldReturnMockResponse() throws Exception {
        SyncRequestDto request = new SyncRequestDto(
                "device-001",
                UUID.randomUUID(),
                UUID.randomUUID(),
                List.of(
                        new SyncRequestDto.OfflineRecordDto(UUID.randomUUID(), Instant.now()),
                        new SyncRequestDto.OfflineRecordDto(UUID.randomUUID(), Instant.now())
                )
        );

        mockMvc.perform(post("/v1/validations/sync")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.validatorDeviceId").value("device-001"))
                .andExpect(jsonPath("$.totalSynced").value(2))
                .andExpect(jsonPath("$.conflictsDetected").value(0))
                .andExpect(jsonPath("$.results").isArray());
    }

    @Test
    @DisplayName("POST /v1/validations - returns 422 for invalid request")
    @WithMockUser(roles = "DOOR_VALIDATOR")
    void validateTicket_shouldReturn422_whenMissingFields() throws Exception {
        String invalidRequest = """
                {
                    "validatorDeviceId": "device-001"
                }
                """;

        mockMvc.perform(post("/v1/validations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }
}