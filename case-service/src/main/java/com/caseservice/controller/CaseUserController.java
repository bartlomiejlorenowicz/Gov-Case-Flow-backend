package com.caseservice.controller;

import com.caseservice.dto.request.CreateCaseRequest;
import com.caseservice.dto.response.CaseEntityDto;
import com.caseservice.dto.response.CaseResponse;
import com.caseservice.security.CurrentUser;
import com.caseservice.security.CurrentUserProvider;
import com.caseservice.service.CaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cases")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class CaseUserController {

    private final CaseService caseService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    public ResponseEntity<CaseResponse> createCase(
            @Valid @RequestBody CreateCaseRequest createCaseRequest
    ) {
        CurrentUser user = currentUserProvider.getCurrentUser();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(caseService.createCase(createCaseRequest, user.userId()));
    }

    @GetMapping
    public ResponseEntity<Page<CaseEntityDto>> getMyCases(
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        CurrentUser user = currentUserProvider.getCurrentUser();
        return ResponseEntity.ok(caseService.getAllForUser(user.userId(), pageable));
    }

    @GetMapping("/{caseId}")
    public ResponseEntity<CaseEntityDto> getCaseById(@PathVariable UUID caseId) {
        return ResponseEntity.ok().body(caseService.getById(caseId));
    }
}
