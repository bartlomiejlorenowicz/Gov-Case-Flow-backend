package com.caseservice.domain;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;

public final class CaseStatusTransitions {

    private static final EnumMap<CaseStatus, Set<CaseStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(CaseStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(CaseStatus.DRAFT,
                EnumSet.of(CaseStatus.SUBMITTED));

        ALLOWED_TRANSITIONS.put(CaseStatus.SUBMITTED,
                EnumSet.of(CaseStatus.IN_REVIEW));

        ALLOWED_TRANSITIONS.put(CaseStatus.IN_REVIEW,
                EnumSet.of(CaseStatus.DECISION_PENDING));

        ALLOWED_TRANSITIONS.put(CaseStatus.DECISION_PENDING,
                EnumSet.of(CaseStatus.APPROVED, CaseStatus.REJECTED));

        ALLOWED_TRANSITIONS.put(CaseStatus.APPROVED,
                EnumSet.of(CaseStatus.CLOSED));

        ALLOWED_TRANSITIONS.put(CaseStatus.REJECTED,
                EnumSet.of(CaseStatus.CLOSED));

        ALLOWED_TRANSITIONS.put(CaseStatus.CLOSED,
                EnumSet.noneOf(CaseStatus.class));
    }

    private CaseStatusTransitions() {}

    public static boolean isAllowedTransition(CaseStatus from, CaseStatus to) {
        return ALLOWED_TRANSITIONS.getOrDefault(from, EnumSet.noneOf(CaseStatus.class)).contains(to);
    }
}
