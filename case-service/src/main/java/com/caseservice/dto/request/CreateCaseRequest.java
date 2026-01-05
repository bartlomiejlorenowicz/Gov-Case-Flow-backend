package com.caseservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateCaseRequest(

        @NotBlank
        String caseNumber,

        @Pattern(regexp = "\\d{11}")
        String applicantPesel
) {}
