package com.auditservice.service;

import com.auditservice.domain.AuditEntry;
import com.auditservice.repository.AuditRepository;
import com.auditservice.event.CaseStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditRepository repository;

    public void save(CaseStatusChangedEvent event) {
        log.info("Saving audit entry for caseId={}, {} -> {}",
                event.caseId(), event.oldStatus(), event.newStatus());
        repository.save(
                AuditEntry.builder()
                        .caseId(event.caseId())
                        .oldStatus(event.oldStatus())
                        .newStatus(event.newStatus())
                        .changedAt(event.changedAt())
                        .changedBy(event.changedBy())
                        .build()
        );
        log.info("Audit entry saved successfully for caseId={}", event.caseId());
    }
}
