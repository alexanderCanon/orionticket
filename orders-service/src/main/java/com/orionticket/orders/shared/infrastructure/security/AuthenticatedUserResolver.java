package com.orionticket.orders.shared.infrastructure.security;

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
        return new AuthenticatedUser(userId, role);
    }

    public UUID currentUserId() {
        return currentUser().userId();
    }

    public void requireSelfOrSupport(UUID buyerId) {
        AuthenticatedUser currentUser = currentUser();
        if (currentUser.isSuperAdmin() || currentUser.isSupport() || currentUser.userId().equals(buyerId)) {
            return;
        }
        throw new AccessDeniedException("Caller does not have access to this buyer's orders");
    }
}
