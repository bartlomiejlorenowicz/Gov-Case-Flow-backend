package com.caseservice.service;

import com.caseservice.domain.CaseEntity;
import com.caseservice.domain.CaseStatus;
import com.caseservice.domain.CaseStatusTransitions;
import com.caseservice.dto.request.CreateCaseRequest;
import com.caseservice.dto.response.CaseResponse;
import com.caseservice.exceptions.InvalidCaseStatusTransitionException;
import com.caseservice.repository.CaseRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CaseService {

    private final CaseRepository caseRepository;

    public CaseResponse createCase(CreateCaseRequest createCaseRequest) {

        if (caseRepository.existsByCaseNumber(createCaseRequest.caseNumber())) {
            throw new IllegalArgumentException("Case with the same case number already exists");
        }

        CaseEntity caseEntity = CaseEntity.builder()
                .caseNumber(createCaseRequest.caseNumber())
                .applicantPesel(createCaseRequest.applicantPesel())
                .status(CaseStatus.SUBMITTED)
                .build();

        caseRepository.save(caseEntity);

        return map(caseEntity);
    }

    public void changeStatus(UUID caseId, CaseStatus newStatus) {

        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new IllegalArgumentException("Case with id " + caseId + " not found"));

        CaseStatus currentStatus = caseEntity.getStatus();

        if (!CaseStatusTransitions.isAllowedTransition(currentStatus, newStatus)) {
            throw new InvalidCaseStatusTransitionException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }

        caseEntity.setStatus(newStatus);
        caseRepository.save(caseEntity);
    }

    private CaseResponse map(CaseEntity caseEntity) {
        return new CaseResponse(
                caseEntity.getId(),
                caseEntity.getCaseNumber(),
                caseEntity.getStatus(),
                caseEntity.getApplicantPesel(),
                caseEntity.getCreatedAt()
        );
    }
}
