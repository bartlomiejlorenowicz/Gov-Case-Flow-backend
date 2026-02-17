package com.caseservice.service;

import com.caseservice.exceptions.*;
import com.caseservice.mapper.CaseStatusEventMapper;
import com.govcaseflow.events.cases.CaseStatusChangedEvent;
import com.caseservice.domain.CaseEntity;
import com.caseservice.domain.CaseStatus;
import com.caseservice.domain.CaseStatusHistory;
import com.caseservice.domain.CaseStatusTransitions;
import com.caseservice.dto.request.CreateCaseRequest;
import com.caseservice.dto.response.CaseEntityDto;
import com.caseservice.dto.response.CaseResponse;
import com.caseservice.mapper.CaseMapper;
import com.caseservice.repository.CaseRepository;
import com.caseservice.repository.CaseStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class CaseService {

    private final CaseRepository caseRepository;

    private final CaseMapper mapper;

    private final Clock clock;

    private final CaseStatusHistoryRepository historyRepository;

    private final ApplicationEventPublisher eventPublisher;

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
    public Page<CaseEntityDto> getAllForUser(UUID userId, Pageable pageable) {
        return caseRepository.findAllByCreatedByUserId(userId, pageable)
                .map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<CaseEntityDto> getAll(Pageable pageable) {
        return caseRepository.findAll(pageable)
                .map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public CaseEntityDto getById(UUID id) {
        CaseEntity caseEntity = caseRepository.findById(id)
                .orElseThrow(() -> new CaseNotFoundException("Case with id " + id + " not found"));
        return mapper.toDto(caseEntity);
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

    @Transactional
    public CaseEntityDto assignToMe(UUID caseId, UUID officerId) {

        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new CaseNotFoundException("Case with id " + caseId + " not found"));

        if (caseEntity.getStatus() != CaseStatus.SUBMITTED) {
            throw new CaseAssignmentNotAllowedException("Only SUBMITTED cases can be assigned");
        }

        if (caseEntity.getAssignedOfficerId() != null) {
            throw new CaseAlreadyAssignedException(
                    "Case is already assigned to officerId=" + caseEntity.getAssignedOfficerId()
            );
        }

        caseEntity.setAssignedOfficerId(officerId);
        caseEntity.setAssignedAt(Instant.now(clock));

        log.info("Case {} assigned to officer {}", caseId, officerId);

        return mapper.toDto(caseEntity);
    }

    @Transactional(readOnly = true)
    public Page<CaseEntityDto> getAssignedToMe(UUID officerId, Pageable pageable) {
        return caseRepository.findAllByAssignedOfficerId(officerId, pageable)
                .map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<CaseEntityDto> getSubmittedQueue(Pageable pageable) {
        return caseRepository.findAllByStatus(CaseStatus.SUBMITTED, pageable)
                .map(mapper::toDto);
    }

    @Transactional
    public void changeStatus(UUID caseId, CaseStatus newStatus, UUID actorUserId, boolean isAdmin) {

        log.info("Changing case {} status to {} by user {}", caseId, newStatus, actorUserId);

        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> new CaseNotFoundException("Case with id " + caseId + " not found"));

        if (!isAdmin) {
            UUID assignedOfficerId = caseEntity.getAssignedOfficerId();
            if (assignedOfficerId == null || !assignedOfficerId.equals(actorUserId)) {
                throw new CaseAccessDeniedException("Officer can change status only for assigned cases");
            }
        }

        CaseStatus oldStatus = caseEntity.getStatus();

        if (!CaseStatusTransitions.isAllowedTransition(oldStatus, newStatus)) {
            throw new InvalidCaseStatusTransitionException(
                    "Invalid status transition from " + oldStatus + " to " + newStatus
            );
        }

        CaseStatusHistory history = CaseStatusHistory.builder()
                .caseId(caseId)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedAt(Instant.now(clock))
                .changedBy(actorUserId.toString())
                .build();

        historyRepository.save(history);

        caseEntity.setStatus(newStatus);

        var event = new CaseStatusChangedEvent(
                caseId,
                CaseStatusEventMapper.toEvent(oldStatus),
                CaseStatusEventMapper.toEvent(newStatus),
                Instant.now(clock),
                actorUserId.toString()
        );

        eventPublisher.publishEvent(event);
    }

}
