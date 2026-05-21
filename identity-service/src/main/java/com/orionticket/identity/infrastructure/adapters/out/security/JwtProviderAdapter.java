package com.orionticket.identity.infrastructure.adapters.out.security;

import com.orionticket.identity.application.port.out.JwtProviderPort;
import com.orionticket.identity.domain.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtProviderAdapter implements JwtProviderPort {

    private final SecretKey key;
    private final long expirationMs;

    public JwtProviderAdapter(
            @Value("${jwt.secret:defaultSecretKeyThatMustBeVeryLongAndSecureForHS256AlgorithmXYZ}") String secret,
            @Value("${jwt.expiration:86400000}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    @Override
    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(user.getUserId().toString())
                .claim("email", user.getEmail())
                .claim("fullName", user.getFullName())
                .claim("roleId", user.getRoleId() != null ? user.getRoleId().toString() : null)
                .claim("organizerId", user.getOrganizerId() != null ? user.getOrganizerId().toString() : null)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }
}
