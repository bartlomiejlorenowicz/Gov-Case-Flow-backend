package com.auditservice.domain;

import jakarta.persistence.*;
import lombok.*;
import com.govcaseflow.events.cases.CaseStatus;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_log")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEntry {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID caseId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CaseStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CaseStatus newStatus;

    @Column(nullable = false)
    private Instant changedAt;

    @Column(nullable = false)
    private String changedBy;

    @Column(nullable = false)
    private String traceId;

    @Enumerated(EnumType.STRING)
    private AuditAction action;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditEventType eventType;

    @Column(nullable = false)
    private String sourceService;

    @Column(nullable = false)
    private String actorUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditTargetType targetType;

    @Column(nullable = false)
    private String targetId;

}