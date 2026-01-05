package com.caseservice.dto.response;

import com.caseservice.domain.CaseStatus;

import java.time.Instant;
import java.util.UUID;

public record CaseResponse(
        UUID id,
        String caseNumber,
        CaseStatus status,
        String applicantPesel,
        Instant createdAt
) {}
