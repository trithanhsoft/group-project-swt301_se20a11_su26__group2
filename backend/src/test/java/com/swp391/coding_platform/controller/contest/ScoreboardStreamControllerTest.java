package com.swp391.coding_platform.controller.contest;

import com.swp391.coding_platform.dto.response.ContestScoreboardResponse;
import com.swp391.coding_platform.entity.contest.ContestEntity;
import com.swp391.coding_platform.repository.contest.ContestRepository;
import com.swp391.coding_platform.service.contest.ContestRankingService;
import com.swp391.coding_platform.service.contest.SseScoreboardManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ScoreboardStreamController.class)

public class ScoreboardStreamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContestRankingService contestRankingService;

    @MockBean
    private SseScoreboardManager sseScoreboardManager;

    @MockBean
    private ContestRepository contestRepository;

    @MockBean
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @MockBean
    private com.swp391.coding_platform.repository.user.UserDailyActivityRepository userDailyActivityRepository;

    @Test
    void testGetScoreboard_AdminAccess() throws Exception {
        ContestEntity contest = new ContestEntity();
        contest.setStartTime(Instant.now().minus(1, ChronoUnit.HOURS));
        contest.setEndTime(Instant.now().plus(1, ChronoUnit.HOURS));

        when(contestRepository.findById(1)).thenReturn(Optional.of(contest));
        
        ContestScoreboardResponse mockResponse = ContestScoreboardResponse.builder().build();
        when(contestRankingService.getScoreboard(1, true)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/contests/1/scoreboard")
                .param("live", "true")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Get contest scoreboard successfully"));
    }

    @Test
    void testGetScoreboard_UserAccess() throws Exception {
        ContestEntity contest = new ContestEntity();
        contest.setStartTime(Instant.now().minus(1, ChronoUnit.HOURS));
        contest.setEndTime(Instant.now().plus(1, ChronoUnit.HOURS));

        when(contestRepository.findById(1)).thenReturn(Optional.of(contest));
        when(contestRepository.isUserRegistered(1, 100)).thenReturn(true);

        ContestScoreboardResponse mockResponse = ContestScoreboardResponse.builder().build();
        when(contestRankingService.getScoreboard(1, false)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/contests/1/scoreboard")
                .param("live", "false")
                .with(jwt().jwt(j -> j.claim("userId", 100)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000));
    }

    @Test
    void testGetScoreboardStream_AdminAccess() throws Exception {
        ContestEntity contest = new ContestEntity();
        contest.setStartTime(Instant.now().minus(1, ChronoUnit.HOURS));
        contest.setEndTime(Instant.now().plus(1, ChronoUnit.HOURS));

        when(contestRepository.findById(1)).thenReturn(Optional.of(contest));
        when(sseScoreboardManager.createConnection(1)).thenReturn(new org.springframework.web.servlet.mvc.method.annotation.SseEmitter());

        mockMvc.perform(get("/api/v1/contests/1/scoreboard/stream")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    void testGetScoreboardStream_UserAccess_Ongoing() throws Exception {
        ContestEntity contest = new ContestEntity();
        contest.setStartTime(Instant.now().minus(1, ChronoUnit.HOURS));
        contest.setEndTime(Instant.now().plus(1, ChronoUnit.HOURS));

        when(contestRepository.findById(1)).thenReturn(Optional.of(contest));
        when(contestRepository.isUserRegistered(1, 100)).thenReturn(true);
        when(sseScoreboardManager.createConnection(1)).thenReturn(new org.springframework.web.servlet.mvc.method.annotation.SseEmitter());

        mockMvc.perform(get("/api/v1/contests/1/scoreboard/stream")
                .with(jwt().jwt(j -> j.claim("userId", 100)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }
}
