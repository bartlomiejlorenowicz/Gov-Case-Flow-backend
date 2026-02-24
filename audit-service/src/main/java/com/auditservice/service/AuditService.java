package com.auditservice.service;

import com.auditservice.domain.*;
import com.auditservice.dto.response.AuditEntryDto;
import com.govcaseflow.events.auth.UserPromotedEvent;
import com.govcaseflow.events.auth.AccountLockedEvent;
import com.govcaseflow.events.auth.UserRegisteredEvent;
import com.auditservice.mapper.AuditEntryMapper;
import com.auditservice.repository.AuditRepository;
import com.govcaseflow.events.cases.AuditSourceService;
import com.govcaseflow.events.cases.CaseStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditRepository repository;
    private final AuditEntryMapper mapper;

    @Transactional
    public void save(CaseStatusChangedEvent event) {
        String traceId = currentTraceId();

        if (repository.existsByTraceIdAndEventType(traceId, AuditEventType.CASE_STATUS_CHANGED)) {
            log.warn("Duplicate audit ignored traceId={}", traceId);
            return;
        }

        AuditSeverity severity = classifySeverity(event);

        log.info("audit.save caseId={} {}->{} severity={}",
                event.caseId(), event.oldStatus(), event.newStatus(), severity);

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
                        .sourceService(AuditSourceService.CASE_SERVICE.value())
                        .actorUserId(event.changedBy())
                        .targetType(AuditTargetType.CASE)
                        .targetId(event.caseId().toString())
                        .build()
        );

        if (severity == AuditSeverity.HIGH) {
            log.error("HIGH severity audit detected caseId={}", event.caseId());
        }
    }

    @Transactional
    public void saveUserRegistered(UserRegisteredEvent event) {
        saveUserEvent(
                AuditEventType.USER_REGISTERED,
                AuditSeverity.INFO,
                event.email(),
                event.userId().toString(),
                event.registeredAt()
        );
    }

    @Transactional
    public void saveUserPromoted(UserPromotedEvent event) {
        saveUserEvent(
                AuditEventType.USER_PROMOTED,
                AuditSeverity.MEDIUM,
                event.actorId().toString(),
                event.targetUserId().toString(),
                event.occurredAt()
        );
    }

    @Transactional
    public void saveAccountLocked(AccountLockedEvent event) {
        saveUserEvent(
                AuditEventType.ACCOUNT_LOCKED,
                AuditSeverity.HIGH,
                "SYSTEM",
                event.userId().toString(),
                event.lockUntil()
        );
    }

    private void saveUserEvent(
            AuditEventType type,
            AuditSeverity severity,
            String actor,
            String target,
            Instant timestamp
    ) {
        String traceId = currentTraceId();

        if (repository.existsByTraceIdAndEventType(traceId, type)) {
            log.warn("Duplicate audit ignored traceId={} type={}", traceId, type);
            return;
        }

        repository.save(
                AuditEntry.builder()
                        .caseId(null)
                        .oldStatus(null)
                        .newStatus(null)
                        .changedAt(timestamp)
                        .changedBy(actor)
                        .traceId(traceId)

                        .eventType(type)
                        .severity(severity)
                        .sourceService(AuditSourceService.AUTH_SERVICE.value())
                        .actorUserId(actor)
                        .targetType(AuditTargetType.USER)
                        .targetId(target)
                        .build()
        );
    }

    public Page<AuditEntryDto> getByTraceId(String traceId, Pageable pageable) {
        return repository.findAllByTraceId(traceId, pageable)
                .map(mapper::toDto);
    }

    public Page<AuditEntryDto> getByCaseId(UUID caseId, Pageable pageable) {
        return repository.findAllByCaseId(caseId, pageable)
                .map(mapper::toDto);
    }

    public Page<AuditEntryDto> getByUserId(String userId, Pageable pageable) {
        return repository.findAllByActorUserId(userId, pageable)
                .map(mapper::toDto);
    }

    public Page<AuditEntryDto> getAllFiltered(AuditSeverity severity, Pageable pageable) {
        if (severity == null) {
            return repository.findAll(pageable).map(mapper::toDto);
        }
        return repository.findAllBySeverity(severity, pageable)
                .map(mapper::toDto);
    }

    public List<EventStatsDto> getEventStats() {
        Instant now = Instant.now();
        Instant from = now.minus(24, ChronoUnit.HOURS);

        return repository.countEventsBetween(from, now)
                .stream()
                .map(e -> new EventStatsDto(e.getEventType(), e.getCount()))
                .toList();
    }

    private static AuditSeverity classifySeverity(CaseStatusChangedEvent event) {
        return switch (event.newStatus()) {
            case REJECTED -> AuditSeverity.HIGH;
            case APPROVED -> AuditSeverity.MEDIUM;
            default -> AuditSeverity.LOW;
        };
    }

    private String currentTraceId() {
        String traceId = MDC.get("traceId");
        return traceId != null ? traceId : UUID.randomUUID().toString();
    }
}