package com.caseservice.security;

import java.util.Set;
import java.util.UUID;

public record CurrentUser(
        UUID userId,
        String username,
        Set<String> roles
) {
    public boolean hasRole(String role) {
        return roles.contains(role);
    }
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }
}
