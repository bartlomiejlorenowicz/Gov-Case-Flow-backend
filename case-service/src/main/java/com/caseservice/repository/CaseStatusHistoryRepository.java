package com.caseservice.repository;

import com.caseservice.domain.CaseStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CaseStatusHistoryRepository
        extends JpaRepository<CaseStatusHistory, UUID> {
}
