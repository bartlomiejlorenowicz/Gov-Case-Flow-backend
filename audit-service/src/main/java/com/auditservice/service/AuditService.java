package com.auditservice.service;

import com.auditservice.config.AuditConstants;
import com.auditservice.domain.*;
import com.auditservice.dto.response.AuditEntryDto;
import com.auditservice.events.UserPromotedEvent;
import com.auditservice.events.UserRegisteredEvent;
import com.auditservice.mapper.AuditEntryMapper;
import com.auditservice.repository.AuditRepository;
import com.govcaseflow.events.cases.CaseStatus;
import com.govcaseflow.events.cases.CaseStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

        String traceId = MDC.get("traceId");

        AuditSeverity severity = classifySeverity(event);

        repository.save(
                AuditEntry.builder()
                        .caseId(event.caseId())
                        .oldStatus(event.oldStatus())
                        .newStatus(event.newStatus())
                        .changedAt(event.changedAt())
                        .changedBy(event.changedBy())
                        .traceId(traceId)
                        .action(AuditAction.CASE_STATUS_CHANGED)

                        .severity(severity)
                        .eventType(AuditEventType.CASE_STATUS_CHANGED)
                        .sourceService(AuditConstants.SOURCE_CASE_SERVICE)
                        .actorUserId(event.changedBy())
                        .targetType(AuditTargetType.CASE)
                        .targetId(event.caseId().toString())

                        .build()
        );

        if (severity == AuditSeverity.HIGH) {
            log.error("HIGH severity audit event detected! caseId={}", event.caseId());
        }

        log.info("Audit entry saved successfully for caseId={}", event.caseId());
    }


    public Page<AuditEntryDto> getByTraceId(String traceId, Pageable pageable) {
        return repository.findAllByTraceId(traceId, pageable)
                .map(mapper::toDto);
    }

    public Page<AuditEntryDto> getAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    public Page<AuditEntryDto> getByCaseId(UUID caseId, Pageable pageable) {
        return repository.findAllByCaseId(caseId, pageable).map(mapper::toDto);
    }

    public Page<AuditEntryDto> getByUserId(String userId, Pageable pageable) {
        return repository.findAllByActorUserId(userId, pageable)
                .map(mapper::toDto);
    }

    public List<EventStatsDto> getEventStats() {
        return repository.countEventsByType().stream()
                .map(r -> new EventStatsDto((String) r[0], (Long) r[1]))
                .toList();
    }

    public Page<AuditEntryDto> getAllFiltered(AuditSeverity severity, Pageable pageable) {
        if (severity == null) {
            return repository.findAll(pageable).map(mapper::toDto);
        }
        return repository.findAllBySeverity(severity, pageable)
                .map(mapper::toDto);
    }

    @Transactional
    public void saveUserRegistered(UserRegisteredEvent event) {

        AuditEntry entry = AuditEntry.builder()
                .caseId(UUID.randomUUID())
                .oldStatus(CaseStatus.UNKNOWN)
                .newStatus(CaseStatus.UNKNOWN)
                .changedAt(event.registeredAt())
                .changedBy(event.email())

                .eventType(AuditEventType.USER_REGISTERED)
                .severity(AuditSeverity.INFO)
                .sourceService("auth-service")
                .actorUserId(event.userId().toString())
                .targetType(AuditTargetType.USER)
                .targetId(event.userId().toString())
                .traceId("N/A")
                .build();

        repository.save(entry);
    }

    @Transactional
    public void saveUserPromoted(UserPromotedEvent event) {

        AuditEntry entry = AuditEntry.builder()
                .caseId(UUID.randomUUID())
                .oldStatus(CaseStatus.UNKNOWN)
                .newStatus(CaseStatus.UNKNOWN)
                .changedAt(event.occurredAt())
                .changedBy(event.actorId().toString())

                .eventType(AuditEventType.USER_PROMOTED)
                .severity(AuditSeverity.MEDIUM)
                .sourceService("auth-service")
                .actorUserId(event.actorId().toString())
                .targetType(AuditTargetType.USER)
                .targetId(event.targetUserId().toString())
                .traceId("N/A")
                .build();

        repository.save(entry);
    }

    private AuditSeverity classifySeverity(CaseStatusChangedEvent event) {
        return switch (event.newStatus()) {
            case REJECTED -> AuditSeverity.HIGH;
            case APPROVED -> AuditSeverity.MEDIUM;
            default -> AuditSeverity.LOW;
        };
    }
}
