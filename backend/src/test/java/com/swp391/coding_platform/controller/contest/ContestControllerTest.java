package com.swp391.coding_platform.controller.contest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swp391.coding_platform.dto.request.ContestRegisterRequest;
import com.swp391.coding_platform.dto.request.ContestSearchRequest;
import com.swp391.coding_platform.dto.response.*;
import com.swp391.coding_platform.repository.user.UserDailyActivityRepository;
import com.swp391.coding_platform.service.contest.ContestService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ContestController.class)
@AutoConfigureMockMvc(addFilters = false)
class ContestControllerTest {

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private UserDailyActivityRepository userDailyActivityRepository;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContestService contestService;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Authentication setupSecurityContext(Object principal, String authority) {
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        List<SimpleGrantedAuthority> authorities = authority != null 
                ? List.of(new SimpleGrantedAuthority(authority)) 
                : Collections.emptyList();
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, "credentials", authorities);
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);
        return auth;
    }

    @Test
    void getContests_shouldReturnOk() throws Exception {
        ContestResponse response = ContestResponse.builder()
                .id(1)
                .title("Weekly Contest 1")
                .status("ONGOING")
                .build();
        
        PageResponse<ContestResponse> pageResponse = new PageResponse<>();
        pageResponse.setContent(Collections.singletonList(response));

        when(contestService.getContests(any(ContestSearchRequest.class), any())).thenReturn(pageResponse);

        mockMvc.perform(get("/contests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.content[0].id").value(1))
                .andExpect(jsonPath("$.result.content[0].title").value("Weekly Contest 1"));
    }

    @Test
    void getContests_withJwtClaims_shouldExtractUserId() throws Exception {
        Jwt jwtMock = mock(Jwt.class);
        when(jwtMock.getClaim("userId")).thenReturn(123);
        Authentication auth = setupSecurityContext(jwtMock, "ROLE_USER");

        PageResponse<ContestResponse> pageResponse = new PageResponse<>();
        pageResponse.setContent(Collections.emptyList());

        when(contestService.getContests(any(ContestSearchRequest.class), eq(123))).thenReturn(pageResponse);

        mockMvc.perform(get("/contests").principal(auth))
                .andExpect(status().isOk());

        verify(contestService).getContests(any(ContestSearchRequest.class), eq(123));
    }

    @Test
    void getContests_withJwtNullClaims_shouldPassNullUserId() throws Exception {
        Jwt jwtMock = mock(Jwt.class);
        when(jwtMock.getClaim("userId")).thenReturn(null);
        Authentication auth = setupSecurityContext(jwtMock, "ROLE_USER");

        PageResponse<ContestResponse> pageResponse = new PageResponse<>();
        pageResponse.setContent(Collections.emptyList());

        when(contestService.getContests(any(ContestSearchRequest.class), isNull())).thenReturn(pageResponse);

        mockMvc.perform(get("/contests").principal(auth))
                .andExpect(status().isOk());

        verify(contestService).getContests(any(ContestSearchRequest.class), isNull());
    }

    @Test
    void getBannerContest_shouldReturnOk() throws Exception {
        ContestResponse response = ContestResponse.builder().id(2).title("Banner Contest").build();
        when(contestService.getBannerContest(any())).thenReturn(response);

        mockMvc.perform(get("/contests/banner"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(2))
                .andExpect(jsonPath("$.result.title").value("Banner Contest"));
    }

    @Test
    void getUserStats_shouldReturnOk() throws Exception {
        ContestUserStatsResponse stats = ContestUserStatsResponse.builder().displayName("Alice").score(1500).build();
        when(contestService.getUserStats(any())).thenReturn(stats);

        mockMvc.perform(get("/contests/user-stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.displayName").value("Alice"))
                .andExpect(jsonPath("$.result.score").value(1500));
    }

    @Test
    void getMyContestStats_shouldReturnOk() throws Exception {
        MyContestStatsResponse stats = MyContestStatsResponse.builder().totalContests(5L).build();
        when(contestService.getMyContestStats(any())).thenReturn(stats);

        mockMvc.perform(get("/contests/my-stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.totalContests").value(5));
    }

    @Test
    void getMyContestHistory_shouldReturnOk() throws Exception {
        MyContestHistoryResponse history = MyContestHistoryResponse.builder().title("Match 1").rank(3).build();
        when(contestService.getMyContestHistory(any())).thenReturn(List.of(history));

        mockMvc.perform(get("/contests/my-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].title").value("Match 1"))
                .andExpect(jsonPath("$.result[0].rank").value(3));
    }

    @Test
    void getContestById_shouldReturnOk() throws Exception {
        ContestResponse response = ContestResponse.builder().id(10).title("Specific Match").build();
        when(contestService.getContestById(eq(10), any())).thenReturn(response);

        mockMvc.perform(get("/contests/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.title").value("Specific Match"));
    }

    @Test
    void registerForContest_shouldReturnOk() throws Exception {
        ContestRegisterRequest req = new ContestRegisterRequest("password123");

        mockMvc.perform(post("/contests/10/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Registered for contest successfully"));

        verify(contestService, times(1)).registerForContest(eq(10), any(), any(ContestRegisterRequest.class));
    }

    @Test
    void registerForContest_withNullRequestBody_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/contests/10/register")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Registered for contest successfully"));

        verify(contestService, times(1)).registerForContest(eq(10), any(), isNull());
    }

    @Test
    void getContestProblems_asRoleAdmin_ShouldPassIsAdminTrue() throws Exception {
        Authentication auth = setupSecurityContext("adminPrincipal", "ROLE_ADMIN");
        ContestProblemResponse prob = ContestProblemResponse.builder().problemId(100).build();
        
        when(contestService.getContestProblems(eq(10), any(), eq(true))).thenReturn(List.of(prob));

        mockMvc.perform(get("/contests/10/problems").principal(auth))
                .andExpect(status().isOk());

        verify(contestService).getContestProblems(eq(10), any(), eq(true));
    }

    @Test
    void getContestProblems_asAdmin_ShouldPassIsAdminTrue() throws Exception {
        Authentication auth = setupSecurityContext("adminPrincipal", "ADMIN");
        ContestProblemResponse prob = ContestProblemResponse.builder().problemId(100).build();
        
        when(contestService.getContestProblems(eq(10), any(), eq(true))).thenReturn(List.of(prob));

        mockMvc.perform(get("/contests/10/problems").principal(auth))
                .andExpect(status().isOk());

        verify(contestService).getContestProblems(eq(10), any(), eq(true));
    }

    @Test
    void getContestProblems_asUser_ShouldPassIsAdminFalse() throws Exception {
        Authentication auth = setupSecurityContext("userPrincipal", "ROLE_USER");
        ContestProblemResponse prob = ContestProblemResponse.builder().problemId(100).build();
        
        when(contestService.getContestProblems(eq(10), any(), eq(false))).thenReturn(List.of(prob));

        mockMvc.perform(get("/contests/10/problems").principal(auth))
                .andExpect(status().isOk());

        verify(contestService).getContestProblems(eq(10), any(), eq(false));
    }

    @Test
    void getContestProblemDetail_asRoleAdmin_ShouldPassIsAdminTrue() throws Exception {
        Authentication auth = setupSecurityContext("adminPrincipal", "ROLE_ADMIN");
        ContestProblemDetailResponse detail = ContestProblemDetailResponse.builder().id(100).build();
        
        when(contestService.getContestProblemDetail(eq(10), eq(100), any(), eq(true))).thenReturn(detail);

        mockMvc.perform(get("/contests/10/problems/100").principal(auth))
                .andExpect(status().isOk());

        verify(contestService).getContestProblemDetail(eq(10), eq(100), any(), eq(true));
    }

    @Test
    void getContestSubmissions_asRoleAdmin_ShouldPassIsAdminTrue() throws Exception {
        Authentication auth = setupSecurityContext("adminPrincipal", "ROLE_ADMIN");
        ContestSubmissionResponse sub = ContestSubmissionResponse.builder().id(500).build();
        
        when(contestService.getContestSubmissions(eq(10), any(), eq(true))).thenReturn(List.of(sub));

        mockMvc.perform(get("/contests/10/submissions").principal(auth))
                .andExpect(status().isOk());

        verify(contestService).getContestSubmissions(eq(10), any(), eq(true));
    }
}
