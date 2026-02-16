package com.auditservice.controller;

import com.auditservice.domain.AuditSeverity;
import com.auditservice.domain.EventStatsDto;
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
import org.springframework.data.domain.Page;
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
                "SYSTEM",

                "CASE_STATUS_CHANGED",
                "LOW",
                "case-service",
                "SYSTEM",
                "CASE",
                caseId.toString()
        );

        when(service.getAllFiltered(eq(null), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/audit")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(entryId.toString()));

        verify(service).getAllFiltered(eq(null), any(Pageable.class));
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
                "SYSTEM",

                "CASE_STATUS_CHANGED",
                "LOW",
                "case-service",
                "SYSTEM",
                "CASE",
                caseId.toString()
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

    @Test
    void shouldFilterBySeverity() throws Exception {
        when(service.getAllFiltered(eq(AuditSeverity.HIGH), any()))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/audit")
                        .param("severity", "HIGH"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnAuditEntriesByTraceId() throws Exception {
        String traceId = UUID.randomUUID().toString();
        var dto = sampleDto();

        when(service.getByTraceId(eq(traceId), any()))
                .thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/audit/trace/{traceId}", traceId))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Trace-Id"));

        verify(service).getByTraceId(eq(traceId), any(Pageable.class));
    }

    @Test
    void shouldReturnAuditEntriesByUserId() throws Exception {
        String userId = UUID.randomUUID().toString();
        var dto = sampleDto();

        when(service.getByUserId(eq(userId), any()))
                .thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/audit/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].actorUserId").value(dto.actorUserId()));

        verify(service).getByUserId(eq(userId), any(Pageable.class));
    }

    @Test
    void shouldReturnEventStats() throws Exception {
        var stats = List.of(
                new EventStatsDto("CASE_STATUS_CHANGED", 5L),
                new EventStatsDto("LOGIN_FAILED", 2L)
        );

        when(service.getEventStats()).thenReturn(stats);

        mockMvc.perform(get("/api/audit/stats/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventType").value("CASE_STATUS_CHANGED"))
                .andExpect(jsonPath("$[0].count").value(5));

        verify(service).getEventStats();
    }

    private AuditEntryDto sampleDto() {
        var caseId = UUID.randomUUID();
        return new AuditEntryDto(
                UUID.randomUUID(),
                caseId,
                "SUBMITTED",
                "IN_REVIEW",
                Instant.now(),
                "SYSTEM",
                "CASE_STATUS_CHANGED",
                "LOW",
                "case-service",
                UUID.randomUUID().toString(),
                "CASE",
                caseId.toString()
        );
    }
}