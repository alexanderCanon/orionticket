package com.orionticket.orders.shared.infrastructure.security;

import java.util.UUID;

public record AuthenticatedUser(UUID userId, String role) {

    public boolean isSuperAdmin() {
        return "SUPER_ADMIN".equals(role);
    }

    public boolean isSupport() {
        return "SUPPORT".equals(role);
    }
}
