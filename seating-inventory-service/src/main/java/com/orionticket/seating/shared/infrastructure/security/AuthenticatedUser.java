package com.orionticket.seating.shared.infrastructure.security;

import java.util.UUID;

public record AuthenticatedUser(UUID userId, String role, UUID organizerId) {

    public UUID effectiveOrganizerId() {
        if (organizerId != null) {
            return organizerId;
        }
        if ("ORGANIZER".equals(role)) {
            return userId;
        }
        return null;
    }
}
