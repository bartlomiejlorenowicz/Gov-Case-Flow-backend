package com.caseservice.service;

import com.caseservice.domain.CaseEntity;
import com.caseservice.domain.CaseStatus;
import com.caseservice.domain.CaseStatusHistory;
import com.caseservice.dto.request.CreateCaseRequest;
import com.caseservice.dto.response.CaseEntityDto;
import com.caseservice.dto.response.CaseResponse;
import com.caseservice.event.CaseEventPublisher;
import com.caseservice.exceptions.CaseAlreadyExistsException;
import com.caseservice.exceptions.CaseNotFoundException;
import com.caseservice.exceptions.InvalidCaseStatusTransitionException;
import com.caseservice.mapper.CaseMapper;
import com.caseservice.repository.CaseRepository;
import com.caseservice.repository.CaseStatusHistoryRepository;
import com.govcaseflow.events.cases.CaseStatusChangedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Mock
    private CaseEventPublisher caseEventPublisher;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    void shouldCreateCaseSuccessfully() {
        //given
        CreateCaseRequest request = new CreateCaseRequest("CASE-2026-003", "90010112345");

        CaseEntity savedEntity = CaseEntity.builder()
                .id(UUID.randomUUID())
                .caseNumber(request.caseNumber())
                .applicantPesel(request.applicantPesel())
                .status(CaseStatus.SUBMITTED)
                .createdByUserId(UUID.randomUUID())
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
        CaseResponse result = caseService.createCase(request, savedEntity.getId());

        //then
        assertEquals(response, result);

        verify(caseRepository).existsByCaseNumber(request.caseNumber());
        verify(caseRepository).save(any(CaseEntity.class));
        verify(caseMapper).toResponse(savedEntity);
    }

    @Test
    void shouldReturnPagedCasesForUser() {
        // given
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);

        List<CaseEntity> entities = List.of(
                CaseEntity.builder().id(UUID.randomUUID()).caseNumber("CASE-1").build(),
                CaseEntity.builder().id(UUID.randomUUID()).caseNumber("CASE-2").build()
        );

        Page<CaseEntity> entityPage = new PageImpl<>(entities, pageable, entities.size());

        when(caseRepository.findAllByCreatedByUserId(userId, pageable)).thenReturn(entityPage);

        when(caseMapper.toDto(any(CaseEntity.class))).thenAnswer(inv -> {
            CaseEntity e = inv.getArgument(0);
            return CaseEntityDto.builder()
                    .id(e.getId())
                    .caseNumber(e.getCaseNumber())
                    .build();
        });

        // when
        Page<CaseEntityDto> result = caseService.getAllForUser(userId, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getCaseNumber()).isEqualTo("CASE-1");
        assertThat(result.getTotalElements()).isEqualTo(2);

        verify(caseRepository).findAllByCreatedByUserId(userId, pageable);
    }


    @Test
    void shouldThrowExceptionWhenCaseNumberAlreadyExists() {
        //given
        UUID userId = UUID.randomUUID();
        CreateCaseRequest request = new CreateCaseRequest("CASE-2026-003", "90010112345");

        when(caseRepository.existsByCaseNumber(request.caseNumber())).thenReturn(true);

        //when and then
        assertThrows(CaseAlreadyExistsException.class, () -> caseService.createCase(request, userId));

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
        verify(eventPublisher).publishEvent(any(CaseStatusChangedEvent.class));
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

    @Test
    void shouldChangeCaseStatusAndSaveStatusHistory() {
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
        verify(eventPublisher).publishEvent(any(CaseStatusChangedEvent.class));
    }

}