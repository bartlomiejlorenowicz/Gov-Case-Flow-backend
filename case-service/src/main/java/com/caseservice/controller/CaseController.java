package com.caseservice.controller;

import com.caseservice.domain.CaseStatus;
import com.caseservice.dto.request.ChangeCaseStatusRequest;
import com.caseservice.dto.request.CreateCaseRequest;
import com.caseservice.dto.response.CaseEntityDto;
import com.caseservice.dto.response.CaseResponse;
import com.caseservice.security.CurrentUser;
import com.caseservice.security.CurrentUserProvider;
import com.caseservice.security.UserPrincipal;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cases")
@RequiredArgsConstructor
public class CaseController {

    private final CaseService caseService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    public ResponseEntity<CaseResponse> createCase(
            @Valid @RequestBody CreateCaseRequest createCaseRequest,
            @CurrentSecurityContext(expression = "authentication.principal") UserPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(caseService.createCase(createCaseRequest, principal.userId()));
    }

//    @PatchMapping("/{caseId}/status")
//    public ResponseEntity<Void> changeCaseStatus(@PathVariable UUID caseId, @Valid @RequestBody ChangeCaseStatusRequest  changeCaseStatusRequest) {
//        caseService.changeStatus(caseId, changeCaseStatusRequest.newStatus());
//        return ResponseEntity.noContent().build();
//    }

    @GetMapping
    public ResponseEntity<Page<CaseEntityDto>> getMyCases(
            @AuthenticationPrincipal UserPrincipal principal,
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(caseService.getAllForUser(principal.userId(), pageable));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICER')")
    public ResponseEntity<Page<CaseEntityDto>> getAllCases(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(caseService.getAll(pageable));
    }

    @GetMapping("/{caseId}")
    public ResponseEntity<CaseEntityDto> getCaseById(@PathVariable UUID caseId) {
        return ResponseEntity.ok().body(caseService.getById(caseId));
    }

    @DeleteMapping("/{caseId}")
    public ResponseEntity<Void> delete(@PathVariable UUID caseId) {
        caseService.deleteCase(caseId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/queue/submitted")
    @PreAuthorize("hasAnyRole('OFFICER','ADMIN')")
    public Page<CaseEntityDto> submittedQueue(Pageable pageable) {
        return caseService.getSubmittedQueue(pageable);
    }

    @PostMapping("/{id}/assign-to-me")
    @PreAuthorize("hasAnyRole('OFFICER','ADMIN')")
    public CaseEntityDto assignToMe(@PathVariable UUID id) {
        CurrentUser user = currentUserProvider.getCurrentUser();
        return caseService.assignToMe(id, user.userId());
    }

    @GetMapping("/assigned-to-me")
    @PreAuthorize("hasAnyRole('OFFICER','ADMIN')")
    public Page<CaseEntityDto> assignedToMe(Pageable pageable) {
        CurrentUser user = currentUserProvider.getCurrentUser();
        return caseService.getAssignedToMe(user.userId(), pageable);
    }

    @PatchMapping("/{caseId}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeStatus(
            @PathVariable UUID caseId,
            @RequestBody @Valid ChangeCaseStatusRequest request,
            Authentication authentication
    ) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        UUID actorUserId = principal.userId();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        caseService.changeStatus(caseId, request.newStatus(), actorUserId, isAdmin);
    }


}
