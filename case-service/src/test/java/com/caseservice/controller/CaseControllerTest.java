package com.caseservice.controller;

import com.caseservice.domain.CaseStatus;
import com.caseservice.dto.request.ChangeCaseStatusRequest;
import com.caseservice.dto.request.CreateCaseRequest;
import com.caseservice.dto.response.CaseEntityDto;
import com.caseservice.dto.response.CaseResponse;
import com.caseservice.exceptions.CaseNotFoundException;
import com.caseservice.security.JwtService;
import com.caseservice.security.UserPrincipal;
import com.caseservice.service.CaseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

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

    @MockBean
    private JwtService jwtService;

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

    private UsernamePasswordAuthenticationToken authUser(UUID userId) {
        var principal = new UserPrincipal(userId, "test@test.com");
        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    private UsernamePasswordAuthenticationToken authUser() {
        return authUser(UUID.randomUUID());
    }

    @Test
    void shouldReturnAllCasesWithSuccessful() throws Exception {
        //given
        UUID userId = UUID.randomUUID();
        var auth = authUser(userId);

        when(caseService.getAllForUser(userId)).thenReturn(new ArrayList<>(cases));

        //then
        mockMvc.perform(get("/api/cases")
                        .header("Accept", "application/json")
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].caseNumber").value("CASE-2026-001"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].caseNumber").value("CASE-2026-002"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2));
    }

    @Test
    void shouldReturnEmptyBodyWhenNoCasesExist() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        var auth = authUser(userId);

        when(caseService.getAllForUser(userId)).thenReturn(List.of());

        mockMvc.perform(get("/api/cases")
                        .header("Accept", "application/json")
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty());
    }

    @Test
    void shouldReturn404WhenCaseNotFound() throws Exception {
        // given
        UUID caseId = UUID.randomUUID();
        var auth = authUser();

        when(caseService.getById(caseId))
                .thenThrow(new CaseNotFoundException("Case with id " + caseId + " not found"));

        mockMvc.perform(get("/api/cases/" + caseId)
                        .header("Accept", "application/json")
                        .with(authentication(auth)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateCaseSuccessfully() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        var auth = authUser(userId);

        CreateCaseRequest request =
                new CreateCaseRequest("CASE-2026-003", "90010112377");

        CaseResponse response =
                new CaseResponse(
                        UUID.randomUUID(),
                        "CASE-2026-003",
                        CaseStatus.SUBMITTED,
                        "90010112377",
                        Instant.now()
                );

        when(caseService.createCase(eq(request), eq(userId))).thenReturn(response);

        mockMvc.perform(post("/api/cases")
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.caseNumber").value("CASE-2026-003"))
                .andExpect(jsonPath("$.status").value("SUBMITTED"));
    }

    @Test
    void shouldReturn400WhenCreateCaseRequestIsInvalid() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        var auth = authUser(userId);

        CreateCaseRequest request = new CreateCaseRequest("CASE-2026-003", "900101123"); // invalid

        // when + then
        mockMvc.perform(post("/api/cases")
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(caseService);
    }

    @Test
    void shouldChangeCaseStatusSuccessfully() throws Exception {
        // given
        UUID caseId = UUID.randomUUID();
        var auth = authUser();

        ChangeCaseStatusRequest request = new ChangeCaseStatusRequest(CaseStatus.SUBMITTED);

        // when + then
        mockMvc.perform(patch("/api/cases/{caseId}/status", caseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .with(authentication(auth))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(caseService).changeStatus(caseId, CaseStatus.SUBMITTED);
    }

    @Test
    void shouldReturn400WhenStatusIsNull() throws Exception {
        UUID caseId = UUID.randomUUID();
        UserPrincipal principal = new UserPrincipal(caseId, "test@test.com");
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        String json = """
                {
                    "newStatus": null
                }
                """;

        mockMvc.perform(patch("/api/cases/{caseId}/status", caseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .with(authentication(auth))
                        .content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(caseService);
    }

    @Test
    void shouldReturnMyCases() throws Exception {
        UUID userId = UUID.randomUUID();

        var principal = new UserPrincipal(userId, "test@test.com");
        var auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(caseService.getAllForUser(userId)).thenReturn(cases);

        mockMvc.perform(get("/api/cases")
                        .with(authentication(auth))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].caseNumber").value("CASE-2026-001"))
                .andExpect(jsonPath("$[1].caseNumber").value("CASE-2026-002"));
    }
}