package com.auditservice.listener;

import com.auditservice.service.AuditService;
import com.govcaseflow.events.cases.CaseStatus;
import com.govcaseflow.events.cases.CaseStatusChangedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CaseStatusChangedListenerTest {

    @Mock
    AuditService auditService;

    @InjectMocks
    CaseStatusChangedListener listener;

    @Test
    void shouldDelegateEventToAuditService() {
        CaseStatusChangedEvent event = new CaseStatusChangedEvent(
                UUID.randomUUID(),
                CaseStatus.SUBMITTED,
                CaseStatus.IN_REVIEW,
                Instant.now(),
                "SYSTEM"
        );

        listener.handle(event);

        verify(auditService).save(event);
    }
}
