package com.caseservice.event;

import com.govcaseflow.events.cases.CaseStatusChangedEvent;

public interface CaseEventPublisher {
    void publishStatusChanged(CaseStatusChangedEvent event);
}
