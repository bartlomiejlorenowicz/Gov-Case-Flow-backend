package com.auditservice.controller;

import com.auditservice.domain.AuditEntry;
import com.auditservice.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditRepository repository;

    @GetMapping
    public Page<AuditEntry> getAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @GetMapping("/case/{caseId}")
    public Page<AuditEntry> getByCaseId(@PathVariable UUID caseId, Pageable pageable) {
        return repository.findAllByCaseId(caseId, pageable);
    }
}