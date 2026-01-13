package com.authservice.event;

import java.time.Instant;
import java.util.UUID;

public record UserRegisteredEvent(
        UUID userId,
        String email,
        Instant registeredAt
) {
}
