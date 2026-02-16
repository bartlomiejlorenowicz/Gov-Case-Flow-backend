package com.auditservice.dto.response;

import java.time.Instant;
import java.util.UUID;

public record AuditEntryDto(
        UUID id,
        UUID caseId,
        String oldStatus,
        String newStatus,
        Instant changedAt,
        String changedBy,
        String traceId,
        String eventType,
        String severity,
        String sourceService,
        String actorUserId,
        String targetType,
        String targetId
) {}
