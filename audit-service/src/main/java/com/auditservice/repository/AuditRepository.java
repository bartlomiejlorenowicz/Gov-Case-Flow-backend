package com.auditservice.repository;

import com.auditservice.domain.AuditEntry;
import com.auditservice.domain.AuditEventType;
import com.auditservice.domain.AuditSeverity;
import com.auditservice.domain.EventStats;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AuditRepository extends JpaRepository<AuditEntry, UUID> {

    Page<AuditEntry> findAllByCaseId(UUID caseId, Pageable pageable);

    Page<AuditEntry> findAllByTraceId(String traceId, Pageable pageable);

    Page<AuditEntry> findAllByActorUserId(String actorUserId, Pageable pageable);

    @Query("""
            select e.eventType as eventType, count(e) as count
            from AuditEntry e
            where e.changedAt between :from and :to
            group by e.eventType
            """)
    List<EventStats> countEventsBetween(Instant from, Instant to);

    Page<AuditEntry> findAllBySeverity(AuditSeverity severity, Pageable pageable);

    boolean existsByTraceIdAndEventType(String traceId, AuditEventType type);

}
