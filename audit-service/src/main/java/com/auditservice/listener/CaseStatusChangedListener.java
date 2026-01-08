package com.auditservice.listener;

import com.auditservice.service.AuditService;
import com.caseservice.event.CaseStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CaseStatusChangedListener {

    private final AuditService auditService;

    @EventListener
    public void handle(CaseStatusChangedEvent event) {
        auditService.save(event);
    }
}