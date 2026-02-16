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

                enumName(e.getOldStatus()),
                enumName(e.getNewStatus()),

                e.getChangedAt(),
                e.getChangedBy(),

                enumName(e.getEventType()),
                enumName(e.getSeverity()),
                e.getSourceService(),
                e.getActorUserId(),
                enumName(e.getTargetType()),
                e.getTargetId()
        );
    }

    private String enumName(Enum<?> e) {
        return e != null ? e.name() : null;
    }
}
