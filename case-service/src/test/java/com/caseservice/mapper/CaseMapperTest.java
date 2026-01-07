package com.caseservice.mapper;

import com.caseservice.domain.CaseEntity;
import com.caseservice.domain.CaseStatus;
import com.caseservice.dto.response.CaseEntityDto;
import com.caseservice.dto.response.CaseResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CaseMapperTest {

    private CaseMapper caseMapper;

    @BeforeEach
    void setUp() {
        caseMapper = new CaseMapper();
    }

    @Test
    void shouldMapCaseEntityToCaseEntityDto() {
        // given
        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-01-01T10:15:30Z");

        CaseEntity entity = CaseEntity.builder()
                .id(id)
                .caseNumber("CASE-2026-003")
                .applicantPesel("90010112345")
                .status(CaseStatus.SUBMITTED)
                .createdAt(createdAt)
                .build();

        // when
        CaseEntityDto dto = caseMapper.toDto(entity);

        // then
        assertNotNull(dto);
        assertEquals(id, dto.getId());
        assertEquals("CASE-2026-003", dto.getCaseNumber());
        assertEquals("90010112345", dto.getApplicantPesel());
        assertEquals(CaseStatus.SUBMITTED, dto.getStatus());
        assertEquals(createdAt.toString(), dto.getCreatedAt());
    }

    @Test
    void shouldMapCaseEntityToCaseResponse() {
        // given
        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-01-01T10:15:30Z");

        CaseEntity entity = CaseEntity.builder()
                .id(id)
                .caseNumber("CASE-2026-003")
                .applicantPesel("90010112345")
                .status(CaseStatus.SUBMITTED)
                .createdAt(createdAt)
                .build();

        // when
        CaseResponse response = caseMapper.toResponse(entity);

        // then
        assertNotNull(response);
        assertEquals(id, response.id());
        assertEquals("CASE-2026-003", response.caseNumber());
        assertEquals("90010112345", response.applicantPesel());
        assertEquals(CaseStatus.SUBMITTED, response.status());
        assertEquals(createdAt, response.createdAt());
    }
}
