package com.orionticket.ticketissuance.infrastructure.security;

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

        return new AuthenticatedUser(
                UUID.fromString(jwt.getSubject()),
                jwt.getClaimAsString("role"),
                resolveOrganizerId(jwt)
        );
    }

    public void requireBuyerSelfOrPrivileged(UUID buyerId) {
        AuthenticatedUser currentUser = currentUser();
        if (currentUser.isSuperAdmin()
                || currentUser.isSupport()
                || currentUser.userId().equals(buyerId)) {
            return;
        }
        throw new AccessDeniedException("Caller does not have access to this buyer's tickets");
    }

    public void requireTicketReadAccess(UUID buyerId) {
        AuthenticatedUser currentUser = currentUser();
        if (currentUser.isSuperAdmin()
                || currentUser.isSupport()
                || currentUser.isDoorValidator()
                || currentUser.isVenueStaff()
                || currentUser.userId().equals(buyerId)) {
            return;
        }
        throw new AccessDeniedException("Caller does not have access to this ticket");
    }

    private static UUID resolveOrganizerId(Jwt jwt) {
        String organizerId = jwt.getClaimAsString("organizerId");
        if (organizerId == null || organizerId.isBlank()) {
            return null;
        }
        return UUID.fromString(organizerId);
    }
}
