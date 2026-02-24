package com.govcaseflow.events.auth;

import java.time.Instant;
import java.util.UUID;

public record UserPromotedEvent(
        UUID actorId,
        UUID targetUserId,
        Instant occurredAt
) {}
