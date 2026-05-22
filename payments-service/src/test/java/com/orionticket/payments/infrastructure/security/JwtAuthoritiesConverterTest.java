package com.orionticket.payments.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthoritiesConverterTest {

    private final JwtAuthoritiesConverter converter = new JwtAuthoritiesConverter();

    @Test
    void mapsRoleAndPermissionsClaimsToAuthorities() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject("00000000-0000-0000-0000-000000000001")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .claim("role", "FINANCE")
                .claim("permissions", List.of("payments:read", "reports:read:financial"))
                .build();

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_FINANCE", "payments:read", "reports:read:financial");
    }
}
