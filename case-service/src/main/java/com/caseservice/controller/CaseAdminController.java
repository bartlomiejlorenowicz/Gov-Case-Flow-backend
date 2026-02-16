package com.caseservice.controller;

import com.caseservice.dto.response.CaseEntityDto;
import com.caseservice.service.CaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/cases")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CaseAdminController {

    private final CaseService caseService;

    @GetMapping
    public Page<CaseEntityDto> getAll(Pageable pageable) {
        return caseService.getAll(pageable);
    }

    @DeleteMapping("/{caseId}")
    public ResponseEntity<Void> delete(@PathVariable UUID caseId) {
        caseService.deleteCase(caseId);
        return ResponseEntity.noContent().build();
    }
}
