package com.swp391.coding_platform.controller.ranking;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import com.swp391.coding_platform.repository.user.UserDailyActivityRepository;

import com.swp391.coding_platform.dto.response.RankingUserResponse;
import com.swp391.coding_platform.dto.response.UserRankStatsResponse;
import com.swp391.coding_platform.service.ranking.RankingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RankingController.class)
class RankingControllerTest {

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private UserDailyActivityRepository userDailyActivityRepository;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RankingService rankingService;

    @Test
    void getGlobalRankings_shouldReturnOk() throws Exception {
        RankingUserResponse userResponse = RankingUserResponse.builder()
                .rank(1)
                .userId(1)
                .name("Test User")
                .points(100.0)
                .build();
        
        when(rankingService.getGlobalRankings(any())).thenReturn(Collections.singletonList(userResponse));

        mockMvc.perform(get("/rankings")
                .with(jwt().jwt(builder -> builder.claim("userId", 1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result[0].rank").value(1))
                .andExpect(jsonPath("$.result[0].name").value("Test User"));
    }

    @Test
    void getUserRankStats_shouldReturnOk() throws Exception {
        UserRankStatsResponse statsResponse = UserRankStatsResponse.builder()
                .rank(5)
                .points(200.0)
                .pointsToNextRank(10.0)
                .nextRankUserName("Next User")
                .build();

        when(rankingService.getUserRankStats(eq(1), any())).thenReturn(statsResponse);

        mockMvc.perform(get("/rankings/me")
                .with(jwt().jwt(builder -> builder.claim("userId", 1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.rank").value(5))
                .andExpect(jsonPath("$.result.points").value(200.0))
                .andExpect(jsonPath("$.result.pointsToNextRank").value(10.0));
    }
}
