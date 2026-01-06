package com.caseservice.controller;

import com.caseservice.dto.request.ChangeCaseStatusRequest;
import com.caseservice.dto.request.CreateCaseRequest;
import com.caseservice.dto.response.CaseEntityDto;
import com.caseservice.dto.response.CaseResponse;
import com.caseservice.service.CaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cases")
@RequiredArgsConstructor
public class CaseController {

    private final CaseService caseService;

    @PostMapping
    public ResponseEntity<CaseResponse> createCase(@Valid @RequestBody CreateCaseRequest createCaseRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(caseService.createCase(createCaseRequest));
    }

    @PatchMapping("/{caseId}/status")
    public ResponseEntity<Void> changeCaseStatus(@PathVariable UUID caseId, @RequestBody ChangeCaseStatusRequest  changeCaseStatusRequest) {
        caseService.changeStatus(caseId, changeCaseStatusRequest.newStatus());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all")
    public ResponseEntity<List<CaseEntityDto>> getAll() {
        return ResponseEntity.ok(caseService.getAllCases());
    }

}
