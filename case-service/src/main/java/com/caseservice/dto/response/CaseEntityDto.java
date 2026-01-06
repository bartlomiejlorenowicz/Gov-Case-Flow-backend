package com.caseservice.dto.response;

import com.caseservice.domain.CaseStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public class CaseEntityDto {
    private UUID id;
    private String caseNumber;
    private CaseStatus status;
    private String applicantPesel;
    private Instant createdAt;

}
