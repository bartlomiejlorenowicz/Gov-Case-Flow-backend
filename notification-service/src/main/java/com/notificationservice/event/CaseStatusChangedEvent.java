package com.notificationservice.event;

import com.notificationservice.domain.CaseStatus;

import java.time.Instant;
import java.util.UUID;

public record CaseStatusChangedEvent(
        UUID caseId,
        CaseStatus oldStatus,
        CaseStatus newStatus,
        Instant changedAt,
        String changedBy
) {}
