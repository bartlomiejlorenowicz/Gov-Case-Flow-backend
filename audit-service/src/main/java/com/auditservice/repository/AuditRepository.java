package com.auditservice.repository;

import com.auditservice.domain.AuditEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuditRepository extends JpaRepository<AuditEntry, UUID> {

    Page<AuditEntry> findAllByCaseId(UUID caseId, Pageable pageable);

}
