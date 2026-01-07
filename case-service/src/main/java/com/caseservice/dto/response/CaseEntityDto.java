package com.caseservice.dto.response;

import com.caseservice.domain.CaseStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class CaseEntityDto {

    private UUID id;
    private String caseNumber;
    private CaseStatus status;
    private String applicantPesel;
    private String createdAt;
}
