package com.caseservice.repository;

import com.caseservice.domain.CaseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.caseservice.domain.CaseStatus;

import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CaseRepository extends JpaRepository<CaseEntity, UUID> {

    Optional<CaseEntity> findByCaseNumber(String caseNumber);

    boolean existsByCaseNumber(String caseNumber);

    Page<CaseEntity> findAllByCreatedByUserId(UUID userId, Pageable pageable);

    Page<CaseEntity> findAllByAssignedOfficerId(UUID officerId, Pageable pageable);

    Page<CaseEntity> findAllByStatus(CaseStatus status, Pageable pageable);

}
