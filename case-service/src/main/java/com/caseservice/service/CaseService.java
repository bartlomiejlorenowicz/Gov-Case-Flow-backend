package com.caseservice.service;

import com.caseservice.domain.CaseEntity;
import com.caseservice.domain.CaseStatus;
import com.caseservice.domain.CaseStatusHistory;
import com.caseservice.domain.CaseStatusTransitions;
import com.caseservice.dto.request.CreateCaseRequest;
import com.caseservice.dto.response.CaseEntityDto;
import com.caseservice.dto.response.CaseResponse;
import com.caseservice.event.CaseEventPublisher;
import com.caseservice.event.CaseStatusChangedEvent;
import com.caseservice.exceptions.CaseAlreadyExistsException;
import com.caseservice.exceptions.CaseNotFoundException;
import com.caseservice.exceptions.InvalidCaseStatusTransitionException;
import com.caseservice.mapper.CaseMapper;
import com.caseservice.repository.CaseRepository;
import com.caseservice.repository.CaseStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class CaseService {

    private final CaseRepository caseRepository;

    private final CaseMapper mapper;

    private final Clock clock;

    private final CaseStatusHistoryRepository historyRepository;

    private final CaseEventPublisher caseEventPublisher;

    @Transactional
    public CaseResponse createCase(CreateCaseRequest createCaseRequest, UUID userId) {

        if (caseRepository.existsByCaseNumber(createCaseRequest.caseNumber())) {
            throw new CaseAlreadyExistsException("Case with the same case number already exists");
        }

        CaseEntity caseEntity = CaseEntity.builder()
                .caseNumber(createCaseRequest.caseNumber())
                .applicantPesel(createCaseRequest.applicantPesel())
                .status(CaseStatus.SUBMITTED)
                .createdAt(Instant.now(clock))
                .createdByUserId(userId)
                .build();

        CaseEntity saved = caseRepository.save(caseEntity);

        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CaseEntityDto> getAllForUser(UUID userId) {
        return caseRepository.findAllByCreatedByUserId(userId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CaseEntityDto> getAll() {
        return caseRepository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public CaseEntityDto getById(UUID id) {
        CaseEntity caseEntity = caseRepository.findById(id)
                .orElseThrow(() -> new CaseNotFoundException("Case with id " + id + " not found"));
        return mapper.toDto(caseEntity);
    }

    @Transactional
    public void changeStatus(UUID caseId, CaseStatus newStatus) {
        log.info("Changing case {} status to {}", caseId, newStatus);

        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new CaseNotFoundException("Case with id " + caseId + " not found"));

        CaseStatus oldStatus = caseEntity.getStatus();

        if (!CaseStatusTransitions.isAllowedTransition(oldStatus, newStatus)) {
            throw new InvalidCaseStatusTransitionException("Invalid status transition from " + oldStatus + " to " + newStatus);
        }

        CaseStatusHistory history = CaseStatusHistory.builder()
                .caseId(caseId)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedAt(Instant.now(clock))
                .changedBy("system")
                .build();

        historyRepository.save(history);

        caseEntity.setStatus(newStatus);

        CaseStatusChangedEvent event =
                new CaseStatusChangedEvent(
                        caseId,
                        oldStatus,
                        newStatus,
                        Instant.now(clock),
                        "SYSTEM"
                );

        caseEventPublisher.publishStatusChanged(event);
    }

    @Transactional
    public void deleteCase(UUID caseId) {
        log.info("Deleting case with id {}", caseId);
        CaseEntity entity = caseRepository.findById(caseId)
                .orElseThrow(() ->
                        new CaseNotFoundException("Case with id " + caseId + " not found")
                );
        caseRepository.delete(entity);
    }
}
