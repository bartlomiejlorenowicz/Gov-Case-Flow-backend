package com.caseservice.dto.request;

import com.caseservice.domain.CaseStatus;

public record ChangeCaseStatusRequest(CaseStatus newStatus) {
}
