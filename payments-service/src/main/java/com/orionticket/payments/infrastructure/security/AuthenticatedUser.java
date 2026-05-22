package com.orionticket.payments.infrastructure.security;

import java.util.UUID;

public record AuthenticatedUser(UUID userId, String role, UUID organizerId) {

    public boolean isBuyer() {
        return "BUYER".equals(role);
    }

    public boolean isFinance() {
        return "FINANCE".equals(role);
    }

    public boolean isPlatformOperator() {
        return "PLATFORM_OPERATOR".equals(role);
    }

    public boolean isSuperAdmin() {
        return "SUPER_ADMIN".equals(role);
    }

    public boolean isOrganizer() {
        return "ORGANIZER".equals(role);
    }

    public UUID effectiveOrganizerId() {
        if (organizerId != null) {
            return organizerId;
        }
        if (isOrganizer()) {
            return userId;
        }
        return null;
    }
}
