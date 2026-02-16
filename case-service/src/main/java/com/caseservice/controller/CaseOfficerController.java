package com.caseservice.controller;

import com.caseservice.dto.request.ChangeCaseStatusRequest;
import com.caseservice.dto.response.CaseEntityDto;
import com.caseservice.security.CurrentUser;
import com.caseservice.security.CurrentUserProvider;
import com.caseservice.service.CaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/officer/cases")
@PreAuthorize("hasAnyRole('OFFICER','ADMIN')")
public class CaseOfficerController {

    private final CaseService caseService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/queue/submitted")
    public Page<CaseEntityDto> submittedQueue(Pageable pageable) {
        return caseService.getSubmittedQueue(pageable);
    }

    @PostMapping("/{id}/assign-to-me")
    public CaseEntityDto assignToMe(@PathVariable UUID id) {
        CurrentUser user = currentUserProvider.getCurrentUser();
        return caseService.assignToMe(id, user.userId());
    }

    @GetMapping("/assigned-to-me")
    public Page<CaseEntityDto> assignedToMe(Pageable pageable) {
        CurrentUser user = currentUserProvider.getCurrentUser();
        return caseService.getAssignedToMe(user.userId(), pageable);
    }

    @GetMapping()
    public ResponseEntity<Page<CaseEntityDto>> getAllCases(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(caseService.getAll(pageable));
    }

    @PatchMapping("/{caseId}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeStatus(
            @PathVariable UUID caseId,
            @RequestBody @Valid ChangeCaseStatusRequest request
    ) {
        CurrentUser user = currentUserProvider.getCurrentUser();

        caseService.changeStatus(
                caseId,
                request.newStatus(),
                user.userId(),
                user.isAdmin()
        );
    }
}
