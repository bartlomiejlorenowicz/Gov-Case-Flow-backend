package com.authservice.event;

import java.time.Instant;
import java.util.UUID;

public record UserPromotedEvent(
        UUID actorId,
        UUID targetUserId,
        Instant occurredAt
) {}
