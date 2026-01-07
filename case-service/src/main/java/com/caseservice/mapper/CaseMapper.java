package com.caseservice.mapper;

import com.caseservice.domain.CaseEntity;
import com.caseservice.dto.response.CaseEntityDto;
import com.caseservice.dto.response.CaseResponse;
import org.springframework.stereotype.Component;

@Component
public class CaseMapper {

    public CaseEntityDto toDto(CaseEntity caseEntity) {
        return CaseEntityDto.builder()
                .id(caseEntity.getId())
                .caseNumber(caseEntity.getCaseNumber())
                .applicantPesel(caseEntity.getApplicantPesel())
                .status(caseEntity.getStatus())
                .createdAt(caseEntity.getCreatedAt().toString())
                .build();
    }

    public CaseResponse toResponse(CaseEntity caseEntity) {
        return new CaseResponse(
                caseEntity.getId(),
                caseEntity.getCaseNumber(),
                caseEntity.getStatus(),
                caseEntity.getApplicantPesel(),
                caseEntity.getCreatedAt()
        );
    }
}
