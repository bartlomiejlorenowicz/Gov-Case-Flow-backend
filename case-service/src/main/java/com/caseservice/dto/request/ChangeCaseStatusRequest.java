package com.caseservice.dto.request;

import com.caseservice.domain.CaseStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeCaseStatusRequest( @NotNull CaseStatus newStatus) {
}
