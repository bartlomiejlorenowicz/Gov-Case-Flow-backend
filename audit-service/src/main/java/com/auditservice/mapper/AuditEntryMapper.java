package com.auditservice.mapper;

import com.auditservice.domain.AuditEntry;
import com.auditservice.dto.response.AuditEntryDto;
import org.springframework.stereotype.Component;

@Component
public class AuditEntryMapper {

    public AuditEntryDto toDto(AuditEntry e) {
        return new AuditEntryDto(
                e.getId(),
                e.getCaseId(),
                e.getOldStatus() != null ? e.getOldStatus().name() : null,
                e.getNewStatus() != null ? e.getNewStatus().name() : null,
                e.getChangedAt(),
                e.getChangedBy()
        );
    }
}
