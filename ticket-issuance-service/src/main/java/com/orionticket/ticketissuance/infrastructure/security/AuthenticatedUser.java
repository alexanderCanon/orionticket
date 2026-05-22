package com.orionticket.ticketissuance.infrastructure.security;

import java.util.UUID;

public record AuthenticatedUser(UUID userId, String role, UUID organizerId) {

    public boolean isBuyer() {
        return "BUYER".equals(role);
    }

    public boolean isSupport() {
        return "SUPPORT".equals(role);
    }

    public boolean isDoorValidator() {
        return "DOOR_VALIDATOR".equals(role);
    }

    public boolean isVenueStaff() {
        return "VENUE_STAFF".equals(role);
    }

    public boolean isOrganizer() {
        return "ORGANIZER".equals(role);
    }

    public boolean isPlatformOperator() {
        return "PLATFORM_OPERATOR".equals(role);
    }

    public boolean isSuperAdmin() {
        return "SUPER_ADMIN".equals(role);
    }
}
