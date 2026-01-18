package com.auditservice.controller;

import com.auditservice.dto.response.AuditEntryDto;
import com.auditservice.security.JwtService;
import com.auditservice.security.SecurityConfig;
import com.auditservice.domain.AuditEntry;
import com.auditservice.repository.AuditRepository;
import com.auditservice.service.AuditService;
import com.govcaseflow.events.cases.CaseStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
@AutoConfigureMockMvc(addFilters = false)
class AuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AuditService service;

    @Test
    void shouldReturnAuditEntriesPaginated() throws Exception {
        var caseId = UUID.randomUUID();
        var entryId = UUID.randomUUID();

        var dto = new AuditEntryDto(
                entryId,
                caseId,
                CaseStatus.SUBMITTED.name(),
                CaseStatus.IN_REVIEW.name(),
                Instant.now(),
                "SYSTEM"
        );

        when(service.getAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/audit")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(entryId.toString()))
                .andExpect(jsonPath("$.content[0].caseId").value(caseId.toString()))
                .andExpect(jsonPath("$.content[0].oldStatus").value("SUBMITTED"))
                .andExpect(jsonPath("$.content[0].newStatus").value("IN_REVIEW"));

        verify(service).getAll(any(PageRequest.class));
    }

    @Test
    void shouldReturnAuditEntriesByCaseIdPaginated() throws Exception {
        var caseId = UUID.randomUUID();
        var entryId = UUID.randomUUID();

        var dto = new AuditEntryDto(
                entryId,
                caseId,
                CaseStatus.SUBMITTED.name(),
                CaseStatus.IN_REVIEW.name(),
                Instant.now(),
                "SYSTEM"
        );

        when(service.getByCaseId(eq(caseId), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/audit/case/{caseId}", caseId)
                        .param("page", "0")
                        .param("size", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].caseId").value(caseId.toString()));

        verify(service).getByCaseId(eq(caseId), any(Pageable.class));
    }
}