package com.caseservice.dto.response;

import com.caseservice.domain.CaseStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record CaseResponse(
        UUID id,
        String caseNumber,
        CaseStatus status,
        String applicantPesel,
        Instant createdAt
) {}
