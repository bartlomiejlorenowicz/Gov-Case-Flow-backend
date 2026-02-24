package com.govcaseflow.events.auth;

import java.time.Instant;
import java.util.UUID;

public record AccountLockedEvent(
        UUID userId,
        Instant lockUntil,
        String reason
) {}