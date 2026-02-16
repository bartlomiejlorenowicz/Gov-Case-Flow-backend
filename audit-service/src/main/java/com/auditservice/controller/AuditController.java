package com.auditservice.controller;

import com.auditservice.domain.AuditEntry;
import com.auditservice.dto.response.AuditEntryDto;
import com.auditservice.repository.AuditRepository;
import com.auditservice.service.AuditService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Page<AuditEntryDto>> getAll(
            @ParameterObject
            @PageableDefault(size = 20, sort = "changedAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.getAll(pageable));
    }

    @GetMapping("/case/{caseId}")
    public ResponseEntity<Page<AuditEntryDto>> getByCaseId(
            @PathVariable UUID caseId,
            @ParameterObject
            @PageableDefault(size = 20, sort = "changedAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.getByCaseId(caseId, pageable));
    }

    @GetMapping("/trace/{traceId}")
    public ResponseEntity<Page<AuditEntryDto>> getByTraceId(@PathVariable String traceId, Pageable pageable) {
        return ResponseEntity.ok(service.getByTraceId(traceId, pageable));
    }
}