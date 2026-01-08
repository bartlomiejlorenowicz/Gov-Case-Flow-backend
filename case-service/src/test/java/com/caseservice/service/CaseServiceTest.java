package com.caseservice.service;

import com.caseservice.domain.CaseEntity;
import com.caseservice.domain.CaseStatus;
import com.caseservice.domain.CaseStatusHistory;
import com.caseservice.dto.request.CreateCaseRequest;
import com.caseservice.dto.response.CaseResponse;
import com.caseservice.exceptions.CaseAlreadyExistsException;
import com.caseservice.exceptions.CaseNotFoundException;
import com.caseservice.exceptions.InvalidCaseStatusTransitionException;
import com.caseservice.mapper.CaseMapper;
import com.caseservice.repository.CaseRepository;
import com.caseservice.repository.CaseStatusHistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaseServiceTest {

    @Mock
    private CaseRepository caseRepository;

    @Mock
    CaseMapper caseMapper;

    @InjectMocks
    private CaseService caseService;

    @Mock
    private Clock clock;

    @Mock
    private CaseStatusHistoryRepository historyRepository;


    @Test
    void shouldCreateCaseSuccessfully() {
        //given
        CreateCaseRequest request = new CreateCaseRequest("CASE-2026-003", "90010112345");

        CaseEntity savedEntity = CaseEntity.builder()
                .id(UUID.randomUUID())
                .caseNumber(request.caseNumber())
                .applicantPesel(request.applicantPesel())
                .status(CaseStatus.SUBMITTED)
                .build();

        CaseResponse response = CaseResponse.builder()
                .id(savedEntity.getId())
                .caseNumber(savedEntity.getCaseNumber())
                .applicantPesel(savedEntity.getApplicantPesel())
                .status(savedEntity.getStatus())
                .build();

        // when
        when(caseRepository.existsByCaseNumber(request.caseNumber())).thenReturn(false);
        when(caseRepository.save(any(CaseEntity.class))).thenReturn(savedEntity);
        when(caseMapper.toResponse(savedEntity)).thenReturn(response);

        //then
        CaseResponse result = caseService.createCase(request);

        //then
        assertEquals(response, result);

        verify(caseRepository).existsByCaseNumber(request.caseNumber());
        verify(caseRepository).save(any(CaseEntity.class));
        verify(caseMapper).toResponse(savedEntity);
    }

    @Test
    void shouldThrowExceptionWhenCaseNumberAlreadyExists() {
        //given
        CreateCaseRequest request = new CreateCaseRequest("CASE-2026-003", "90010112345");

        when(caseRepository.existsByCaseNumber(request.caseNumber())).thenReturn(true);

        //when and then
        assertThrows(CaseAlreadyExistsException.class, () -> caseService.createCase(request));

        verify(caseRepository).existsByCaseNumber(request.caseNumber());
        verify(caseRepository, never()).save(any());
        verify(caseMapper, never()).toResponse(any());
    }

    @Test
    void shouldThrowCaseNotFoundExceptionWhenChangingStatusOfNonExistingCase() {
        UUID caseId = UUID.randomUUID();

        when(caseRepository.findById(caseId)).thenReturn(Optional.empty());

        assertThrows(
                CaseNotFoundException.class,
                () -> caseService.changeStatus(caseId, CaseStatus.IN_REVIEW)
        );

        verify(caseRepository).findById(caseId);
    }

    @Test
    void shouldThrowExceptionWhenStatusTransitionIsInvalid() {
        UUID caseId = UUID.randomUUID();

        CaseEntity entity = CaseEntity.builder()
                .id(caseId)
                .status(CaseStatus.APPROVED)
                .build();

        when(caseRepository.findById(caseId)).thenReturn(Optional.of(entity));

        assertThrows(
                InvalidCaseStatusTransitionException.class,
                () -> caseService.changeStatus(caseId, CaseStatus.IN_REVIEW)
        );

        verify(caseRepository).findById(caseId);
    }

    @Test
    void shouldChangeStatusSuccessfullyWhenTransitionIsAllowed() {
        UUID caseId = UUID.randomUUID();

        CaseEntity entity = CaseEntity.builder()
                .id(caseId)
                .status(CaseStatus.SUBMITTED)
                .build();

        when(caseRepository.findById(caseId)).thenReturn(Optional.of(entity));

        caseService.changeStatus(caseId, CaseStatus.IN_REVIEW);

        assertEquals(CaseStatus.IN_REVIEW, entity.getStatus());
    }

    @Test
    void shouldThrowCaseNotFoundExceptionWhenGetByIdDoesNotExist() {
        UUID caseId = UUID.randomUUID();

        when(caseRepository.findById(caseId)).thenReturn(Optional.empty());

        assertThrows(
                CaseNotFoundException.class,
                () -> caseService.getById(caseId)
        );
    }

    @Test
    void shouldThrowCaseNotFoundExceptionWhenDeletingNonExistingCase() {
        UUID caseId = UUID.randomUUID();

        when(caseRepository.findById(caseId)).thenReturn(Optional.empty());

        assertThrows(
                CaseNotFoundException.class,
                () -> caseService.deleteCase(caseId)
        );
    }

    @Test
    void shouldDeleteCaseSuccessfully() {
        UUID caseId = UUID.randomUUID();

        CaseEntity entity = CaseEntity.builder()
                .id(caseId)
                .build();

        when(caseRepository.findById(caseId)).thenReturn(Optional.of(entity));

        caseService.deleteCase(caseId);

        verify(caseRepository).delete(entity);
    }

    @Test
    void shouldSaveStatusHistoryWhenChangingStatus() {
        // given
        UUID caseId = UUID.randomUUID();

        CaseEntity entity = CaseEntity.builder()
                .id(caseId)
                .status(CaseStatus.SUBMITTED)
                .build();

        when(caseRepository.findById(caseId)).thenReturn(Optional.of(entity));

        // when
        caseService.changeStatus(caseId, CaseStatus.IN_REVIEW);

        // then
        assertEquals(CaseStatus.IN_REVIEW, entity.getStatus());

        // then
        verify(historyRepository).save(any(CaseStatusHistory.class));
    }

}