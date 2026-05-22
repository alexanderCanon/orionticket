package com.orionticket.payments.infrastructure.security;

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

    public UUID currentUserId() {
        return currentUser().userId();
    }

    public void requirePaymentReadAccess(UUID buyerId) {
        AuthenticatedUser currentUser = currentUser();
        if (currentUser.isSuperAdmin()
                || currentUser.isFinance()
                || currentUser.isPlatformOperator()
                || currentUser.userId().equals(buyerId)) {
            return;
        }
        throw new AccessDeniedException("Caller does not have access to this payment");
    }

    public UUID resolvePayoutOrganizerScope(UUID requestedOrganizerId) {
        AuthenticatedUser currentUser = currentUser();
        if (currentUser.isFinance() || currentUser.isPlatformOperator() || currentUser.isSuperAdmin()) {
            return requestedOrganizerId;
        }

        UUID organizerId = currentUser.effectiveOrganizerId();
        if (organizerId != null) {
            return organizerId;
        }

        throw new AccessDeniedException("Caller does not have access to payouts");
    }

    public void requirePayoutReadAccess(UUID organizerId) {
        AuthenticatedUser currentUser = currentUser();
        if (currentUser.isFinance() || currentUser.isPlatformOperator() || currentUser.isSuperAdmin()) {
            return;
        }
        UUID scopedOrganizerId = currentUser.effectiveOrganizerId();
        if (scopedOrganizerId != null && scopedOrganizerId.equals(organizerId)) {
            return;
        }
        throw new AccessDeniedException("Caller does not have access to this payout");
    }

    private static UUID resolveOrganizerId(Jwt jwt) {
        String organizerId = jwt.getClaimAsString("organizerId");
        if (organizerId == null || organizerId.isBlank()) {
            return null;
        }
        return UUID.fromString(organizerId);
    }
}
