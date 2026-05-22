package com.orionticket.events.infrastructure.adapters.out.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AuthenticatedUserResolver {

    public AuthenticatedUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new AccessDeniedException("Authenticated JWT is required");
        }

        UUID userId = UUID.fromString(jwt.getSubject());
        String role = jwt.getClaimAsString("role");
        UUID organizerId = resolveOrganizerId(jwt);

        return new AuthenticatedUser(userId, role, organizerId);
    }

    public UUID requireOrganizerId() {
        UUID organizerId = currentUser().effectiveOrganizerId();
        if (organizerId == null) {
            throw new AccessDeniedException("Authenticated user is not scoped to an organizer");
        }
        return organizerId;
    }

    private static UUID resolveOrganizerId(Jwt jwt) {
        String organizerId = jwt.getClaimAsString("organizerId");
        if (organizerId == null || organizerId.isBlank()) {
            return null;
        }
        return UUID.fromString(organizerId);
    }
}
