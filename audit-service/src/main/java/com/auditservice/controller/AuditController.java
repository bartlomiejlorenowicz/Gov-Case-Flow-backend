package com.auditservice.controller;

import com.auditservice.domain.AuditSeverity;
import com.auditservice.domain.EventStatsDto;
import com.auditservice.dto.response.AuditEntryDto;
import com.auditservice.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService service;

    @GetMapping("/case/{caseId}")
    @PreAuthorize("hasAnyRole('OFFICER','ADMIN')")
    public ResponseEntity<Page<AuditEntryDto>> getByCaseId(
            @PathVariable UUID caseId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.getByCaseId(caseId, pageable));
    }

    @GetMapping("/trace/{traceId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditEntryDto>> getByTraceId(
            @PathVariable String traceId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.getByTraceId(traceId, pageable));
    }

    @PreAuthorize("hasAnyRole('ADMIN','OFFICER')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<AuditEntryDto>> getByUser(
            @PathVariable String userId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.getByUserId(userId, pageable));
    }

    @GetMapping("/stats/events")
    @PreAuthorize("hasRole('ADMIN')")
    public List<EventStatsDto> stats() {
        return service.getEventStats();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<AuditEntryDto> getAll(
            @RequestParam(required = false) AuditSeverity severity,
            Pageable pageable
    ) {
        return service.getAllFiltered(severity, pageable);
    }

}