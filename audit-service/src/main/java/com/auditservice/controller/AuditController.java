package com.auditservice.controller;

import com.auditservice.dto.response.AuditEntryDto;
import com.auditservice.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService service;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditEntryDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(service.getAll(pageable));
    }

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
}