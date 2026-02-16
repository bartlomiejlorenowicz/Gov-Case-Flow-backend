package com.caseservice.controller;

import com.caseservice.domain.CaseStatus;
import com.caseservice.dto.request.CreateCaseRequest;
import com.caseservice.dto.response.CaseEntityDto;
import com.caseservice.dto.response.CaseResponse;
import com.caseservice.exceptions.CaseNotFoundException;
import com.caseservice.security.CurrentUser;
import com.caseservice.security.CurrentUserProvider;
import com.caseservice.security.JwtService;
import com.caseservice.security.UserPrincipal;
import com.caseservice.service.CaseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({
        CaseOfficerController.class,
        CaseUserController.class
})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class CaseControllerTest {

    @MockBean
    CaseService caseService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CurrentUserProvider currentUserProvider;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

        var page = new PageImpl<>(cases, PageRequest.of(0, 20), cases.size());
        when(caseService.getAllForUser(eq(userId), any(Pageable.class))).thenReturn(page);
        when(currentUserProvider.getCurrentUser())
                .thenReturn(new CurrentUser(
                        userId,
                        "test@test.com",
                        Set.of("USER")
                ));

        // then
        mockMvc.perform(get("/api/cases")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(authentication(auth)))
                .andExpect(status().isOk())

                // content
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isNotEmpty())
                .andExpect(jsonPath("$.content[0].caseNumber").value("CASE-2026-001"))
                .andExpect(jsonPath("$.content[1].caseNumber").value("CASE-2026-002"))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    void shouldReturnEmptyBodyWhenNoCasesExist() throws Exception {

        UUID userId = UUID.randomUUID();

        when(currentUserProvider.getCurrentUser())
                .thenReturn(new CurrentUser(
                        userId,
                        "user@test.com",
                        Set.of("USER")
                ));

        var emptyPage = new PageImpl<CaseEntityDto>(
                List.of(),
                PageRequest.of(0, 20),
                0
        );

        when(caseService.getAllForUser(eq(userId), any(Pageable.class)))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/api/cases")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
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

        UUID userId = UUID.randomUUID();

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

        when(currentUserProvider.getCurrentUser())
                .thenReturn(new CurrentUser(
                        userId,
                        "test@test.com",
                        Set.of("USER")
                ));

        when(caseService.createCase(eq(request), eq(userId)))
                .thenReturn(response);

        mockMvc.perform(post("/api/cases")
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
    void shouldChangeCaseStatusSuccessfully_asAdmin() throws Exception {

        UUID caseId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        when(currentUserProvider.getCurrentUser())
                .thenReturn(new CurrentUser(
                        adminId,
                        "admin@caseflow.local",
                        Set.of("ADMIN")
                ));

        String json = """
        { "newStatus": "IN_REVIEW" }
        """;

        mockMvc.perform(patch("/api/officer/cases/{caseId}/status", caseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content(json))
                .andExpect(status().isNoContent());

        verify(caseService)
                .changeStatus(caseId, CaseStatus.IN_REVIEW, adminId, true);
    }

    @Test
    void shouldReturn400WhenStatusIsNull() throws Exception {
        UUID caseId = UUID.randomUUID();

        when(currentUserProvider.getCurrentUser())
                .thenReturn(new CurrentUser(
                        UUID.randomUUID(),
                        "admin@test.com",
                        Set.of("ADMIN")
                ));

        String json = """
        { "newStatus": null }
        """;

        mockMvc.perform(patch("/api/officer/cases/{caseId}/status", caseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(caseService);
    }

    @Test
    void shouldReturnMyCasesPaged() throws Exception {

        UUID userId = UUID.randomUUID();

        when(currentUserProvider.getCurrentUser())
                .thenReturn(new CurrentUser(
                        userId,
                        "user@test.com",
                        Set.of("USER")
                ));

        var page = new PageImpl<>(cases, PageRequest.of(0, 20), cases.size());

        when(caseService.getAllForUser(eq(userId), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/cases?page=0&size=20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].caseNumber").value("CASE-2026-001"))
                .andExpect(jsonPath("$.content[1].caseNumber").value("CASE-2026-002"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.number").value(0));
    }
}