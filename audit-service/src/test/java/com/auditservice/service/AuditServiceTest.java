package com.auditservice.service;

import com.auditservice.domain.AuditEntry;
import com.auditservice.mapper.AuditEntryMapper;
import com.auditservice.repository.AuditRepository;
import com.govcaseflow.events.cases.CaseStatus;
import com.govcaseflow.events.cases.CaseStatusChangedEvent;
import com.govcaseflow.infrastructure.tracing.CorrelationId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    private static final String CORRELATION_ID = "3f2b8c1e-6d4a-4f7e-9b2c-1a2b3c4d5e6f";

    @Mock
    AuditRepository repository;

    @Mock
    AuditEntryMapper mapper;

    @InjectMocks
    AuditService service;

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void shouldStoreCorrelationIdFromMdcAsTraceId() {
        MDC.put(CorrelationId.MDC_KEY, CORRELATION_ID);

        service.save(event());

        ArgumentCaptor<AuditEntry> saved = ArgumentCaptor.forClass(AuditEntry.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().getTraceId()).isEqualTo(CORRELATION_ID);
    }

    @Test
    void shouldGenerateTraceIdWhenMdcEmpty() {
        service.save(event());

        ArgumentCaptor<AuditEntry> saved = ArgumentCaptor.forClass(AuditEntry.class);
        verify(repository).save(saved.capture());
        assertThat(CorrelationId.isValid(saved.getValue().getTraceId())).isTrue();
    }

    private static CaseStatusChangedEvent event() {
        return new CaseStatusChangedEvent(
                UUID.randomUUID(),
                CaseStatus.SUBMITTED,
                CaseStatus.IN_REVIEW,
                Instant.now(),
                "SYSTEM"
        );
    }
}
