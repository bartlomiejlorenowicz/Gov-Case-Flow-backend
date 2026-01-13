package com.caseservice.mapper;

import com.caseservice.domain.CaseStatus;

public class CaseStatusEventMapper {

    public static com.govcaseflow.events.cases.CaseStatus toEvent(CaseStatus status) {
        return com.govcaseflow.events.cases.CaseStatus.valueOf(status.name());
    }
}