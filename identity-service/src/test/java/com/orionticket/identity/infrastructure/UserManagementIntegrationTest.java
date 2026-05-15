package com.orionticket.identity.infrastructure;

import com.orionticket.identity.infrastructure.adapters.out.persistence.repository.SpringDataUserRepository;
import com.orionticket.identity.infrastructure.adapters.out.persistence.entity.UserJpaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SpringDataUserRepository userRepository;

    private UUID testUserId;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        // Creamos un usuario de prueba directamente en la BD
        testUserId = UUID.randomUUID();
        UserJpaEntity user = new UserJpaEntity();
        user.setUserId(testUserId);
        user.setEmail("integration@orionticket.com");
        user.setPasswordHash("hashedpassword");
        user.setFullName("Integration Test");
        user.setPhone("12345678");
        user.setStatus("ACTIVE");
        user.setRoleId(UUID.fromString("00000000-0000-0000-0000-000000000001")); // COMPRADOR role from flyway

        userRepository.save(user);
    }

    @Test
    void shouldSuspendUser() throws Exception {
        mockMvc.perform(put("/v1/users/" + testUserId + "/suspend")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUSPENDED"));
    }

    @Test
    void shouldUpdateUserRole() throws Exception {
        String newRoleId = "00000000-0000-0000-0000-000000000002"; // ORGANIZADOR
        String requestBody = "{\"newRoleId\": \"" + newRoleId + "\"}";

        mockMvc.perform(put("/v1/users/" + testUserId + "/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleId").value(newRoleId));
    }
}
