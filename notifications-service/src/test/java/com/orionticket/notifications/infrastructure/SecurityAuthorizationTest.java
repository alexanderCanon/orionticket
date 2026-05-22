package com.orionticket.notifications.infrastructure;

import com.orionticket.notifications.infrastructure.config.JwtAuthoritiesConverter;
import com.orionticket.notifications.infrastructure.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SecurityAuthorizationTest.TestController.class)
@Import({
        SecurityConfig.class,
        JwtAuthoritiesConverter.class,
        SecurityAuthorizationTest.TestController.class
})
@TestPropertySource(properties = {
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://identity-service:8081/.well-known/jwks.json",
        "jwt.issuer=orionticket-identity"
})
class SecurityAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEndpointRemainsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void notificationOperationalEndpointWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/v1/notifications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void notificationOperationalEndpointWithSupportTokenPassesSecurity() throws Exception {
        mockMvc.perform(get("/v1/notifications")
                        .with(jwt().jwt(jwt -> jwt
                                        .subject(UUID.randomUUID().toString())
                                        .claim("role", "SUPPORT"))
                                .authorities(new SimpleGrantedAuthority("ROLE_SUPPORT"))))
                .andExpect(status().isOk());
    }

    @Test
    void notificationOperationalEndpointWithBuyerTokenReturnsForbidden() throws Exception {
        mockMvc.perform(get("/v1/notifications")
                        .with(jwt().jwt(jwt -> jwt
                                        .subject(UUID.randomUUID().toString())
                                        .claim("role", "BUYER"))
                                .authorities(new SimpleGrantedAuthority("ROLE_BUYER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void retryFailedEndpointAllowsPlatformOperator() throws Exception {
        mockMvc.perform(post("/v1/notifications/retry-failed")
                        .with(jwt().jwt(jwt -> jwt
                                        .subject(UUID.randomUUID().toString())
                                        .claim("role", "PLATFORM_OPERATOR"))
                                .authorities(new SimpleGrantedAuthority("ROLE_PLATFORM_OPERATOR"))))
                .andExpect(status().isOk());
    }

    @Test
    void singleRetryEndpointRequiresSupportOrSuperAdmin() throws Exception {
        mockMvc.perform(post("/v1/notifications/" + UUID.randomUUID() + "/retry")
                        .with(jwt().jwt(jwt -> jwt
                                        .subject(UUID.randomUUID().toString())
                                        .claim("role", "PLATFORM_OPERATOR"))
                                .authorities(new SimpleGrantedAuthority("ROLE_PLATFORM_OPERATOR"))))
                .andExpect(status().isForbidden());
    }

    @RestController
    static class TestController {

        @GetMapping("/actuator/health")
        ResponseEntity<Void> health() {
            return ResponseEntity.ok().build();
        }

        @GetMapping("/v1/notifications")
        ResponseEntity<Void> notifications() {
            return ResponseEntity.ok().build();
        }

        @PostMapping("/v1/notifications/retry-failed")
        ResponseEntity<Void> retryFailed() {
            return ResponseEntity.ok().build();
        }

        @PostMapping("/v1/notifications/{notificationId}/retry")
        ResponseEntity<Void> retryOne() {
            return ResponseEntity.ok().build();
        }
    }
}
