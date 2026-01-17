package com.auditservice.service;

import com.auditservice.domain.AuditEntry;
import com.auditservice.dto.response.AuditEntryDto;
import com.auditservice.mapper.AuditEntryMapper;
import com.auditservice.repository.AuditRepository;
import com.govcaseflow.events.cases.CaseStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditRepository repository;
    private final AuditEntryMapper mapper;

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

    public Page<AuditEntryDto> getAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    public Page<AuditEntryDto> getByCaseId(UUID caseId, Pageable pageable) {
        return repository.findAllByCaseId(caseId, pageable).map(mapper::toDto);
    }
}
