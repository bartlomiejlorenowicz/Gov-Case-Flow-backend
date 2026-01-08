package com.auditservice.service;

import com.auditservice.domain.AuditEntry;
import com.auditservice.repository.AuditRepository;
import com.caseservice.event.CaseStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditRepository repository;

    public void save(CaseStatusChangedEvent event) {
        repository.save(
                AuditEntry.builder()
                        .caseId(event.caseId())
                        .oldStatus(event.oldStatus())
                        .newStatus(event.newStatus())
                        .changedAt(event.changedAt())
                        .changedBy(event.changedBy())
                        .build()
        );
    }
}
