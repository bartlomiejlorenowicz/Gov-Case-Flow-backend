package com.caseservice.controller;

import com.caseservice.domain.CaseStatus;
import com.caseservice.dto.request.ChangeCaseStatusRequest;
import com.caseservice.dto.request.CreateCaseRequest;
import com.caseservice.dto.response.CaseEntityDto;
import com.caseservice.dto.response.CaseResponse;
import com.caseservice.exceptions.CaseNotFoundException;
import com.caseservice.service.CaseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CaseController.class)
@ActiveProfiles("test")
class CaseControllerTest {

    @MockBean
    CaseService caseService;

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    List<CaseEntityDto> cases;

    @BeforeEach
    void setUp() {
        cases = List.of(
                CaseEntityDto.builder()
                        .id(UUID.randomUUID())
                        .caseNumber("CASE-2026-001")
                        .status(CaseStatus.SUBMITTED)
                        .applicantPesel("90010112345")
                        .createdAt("2026-01-07T00:38:18.782269Z")
                        .build(),

                CaseEntityDto.builder()
                        .id(UUID.randomUUID())
                        .caseNumber("CASE-2026-002")
                        .status(CaseStatus.IN_REVIEW)
                        .applicantPesel("90010112366")
                        .createdAt("2026-01-09T00:38:18.782269Z")
                        .build());
    }

    @Test
    void shouldReturnAllCasesWithSuccessful() throws Exception {
        //given
        List<CaseEntityDto> expectedCases = new ArrayList<>(cases);

        //when
        when(caseService.getAll()).thenReturn(expectedCases);

        //then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/cases")
                        .header("Accept", "application/json"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].caseNumber").value("CASE-2026-001"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].caseNumber").value("CASE-2026-002"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2));
    }

    @Test
    void shouldReturnEmptyBodyWhenNoCasesExist() throws Exception {
        //given
        List<CaseEntityDto> expectedCases = new ArrayList<>();

        //when
        when(caseService.getAll()).thenReturn(expectedCases);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/cases")
                        .header("Accept", "application/json"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty());
    }

    @Test
    void shouldReturn404WhenCaseNotFound() throws Exception {
        //given
        UUID caseId = UUID.randomUUID();

        //when
        when(caseService.getById(caseId))
                .thenThrow(new CaseNotFoundException("Case with id " + caseId + " not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/cases/" + caseId)
                        .header("Accept", "application/json"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateCaseSuccessfully() throws Exception {
        //given
        CreateCaseRequest createCaseRequest = new CreateCaseRequest("CASE-2026-003", "90010112377");

        CaseResponse caseResponse = new CaseResponse(UUID.randomUUID(), "CASE-2026-003", CaseStatus.SUBMITTED, "90010112377", Instant.now());
        //when
        when(caseService.createCase(createCaseRequest)).thenReturn(caseResponse);

        //then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/cases")
                        .header("Accept", "application/json")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createCaseRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturn400WhenCreateCaseRequestIsInvalid() throws Exception {
        //given
        CreateCaseRequest createCaseRequest = new CreateCaseRequest("CASE-2026-003", "900101123");

        CaseResponse caseResponse = new CaseResponse(UUID.randomUUID(), "CASE-2026-003", CaseStatus.SUBMITTED, "900101123", Instant.now());

        //when
        when(caseService.createCase(createCaseRequest)).thenReturn(caseResponse);

        //then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/cases")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createCaseRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldChangeCaseStatusSuccessfully() throws Exception {
        // given
        UUID caseId = UUID.randomUUID();

        ChangeCaseStatusRequest request =
                new ChangeCaseStatusRequest(CaseStatus.IN_REVIEW);

        // when + then
        mockMvc.perform(patch("/api/cases/{caseId}/status", caseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(caseService).changeStatus(caseId, CaseStatus.IN_REVIEW);
    }

    @Test
    void shouldReturn400WhenStatusIsNull() throws Exception {
        UUID caseId = UUID.randomUUID();

        String json = """
        {
            "newStatus": null
        }
        """;

        mockMvc.perform(patch("/api/cases/{caseId}/status", caseId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(caseService);
    }
}