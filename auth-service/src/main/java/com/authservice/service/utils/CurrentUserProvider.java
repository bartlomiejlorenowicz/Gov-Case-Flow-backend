package com.authservice.service.utils;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CurrentUserProvider {

    public UUID getUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user");
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof UUID uuid) {
            return uuid;
        }

        if ("anonymousUser".equals(principal)) {
            throw new IllegalStateException("Anonymous user not allowed");
        }

        if (principal instanceof String s) {
            try {
                return UUID.fromString(s);
            } catch (IllegalArgumentException ignored) {}
        }

        throw new IllegalStateException("Invalid principal type: " +
                (principal == null ? "null" : principal.getClass().getName()));
    }
}