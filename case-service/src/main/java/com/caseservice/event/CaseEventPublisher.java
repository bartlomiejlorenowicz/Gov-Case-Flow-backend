package com.caseservice.event;

public interface CaseEventPublisher {
    void publishStatusChanged(CaseStatusChangedEvent event);
}
