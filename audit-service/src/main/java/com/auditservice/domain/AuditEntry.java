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
}