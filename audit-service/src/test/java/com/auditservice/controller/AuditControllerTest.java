package com.auditservice.controller;

import com.auditservice.config.SecurityConfig;
import com.auditservice.domain.AuditEntry;
import com.auditservice.repository.AuditRepository;
import com.govcaseflow.events.cases.CaseStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuditController.class)
@Import(SecurityConfig.class)
class AuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditRepository repository;

    @Test
    void shouldReturnAuditEntriesPaginated() throws Exception {
        var caseId = UUID.randomUUID();

        var entry = AuditEntry.builder()
                .id(UUID.randomUUID())
                .caseId(caseId)
                .oldStatus(CaseStatus.SUBMITTED)
                .newStatus(CaseStatus.IN_REVIEW)
                .changedAt(Instant.now())
                .changedBy("SYSTEM")
                .build();

        when(repository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(entry)));

        mockMvc.perform(get("/api/audit")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].caseId").value(caseId.toString()));

        verify(repository).findAll(any(PageRequest.class));
    }

    @Test
    void shouldReturnAuditEntriesByCaseIdPaginated() throws Exception {
        var caseId = UUID.randomUUID();

        var entry = AuditEntry.builder()
                .id(UUID.randomUUID())
                .caseId(caseId)
                .oldStatus(CaseStatus.SUBMITTED)
                .newStatus(CaseStatus.REJECTED)
                .changedAt(Instant.now())
                .changedBy("SYSTEM")
                .build();

        when(repository.findAllByCaseId(eq(caseId), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(entry)));

        mockMvc.perform(get("/api/audit/case/{caseId}", caseId)
                        .param("page", "0")
                        .param("size", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].caseId").value(caseId.toString()));

        verify(repository).findAllByCaseId(eq(caseId), any(PageRequest.class));
    }
}