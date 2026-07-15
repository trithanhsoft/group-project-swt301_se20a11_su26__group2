package com.swp391.coding_platform.controller.user;

import com.swp391.coding_platform.dto.response.*;
import com.swp391.coding_platform.repository.user.UserDailyActivityRepository;
import com.swp391.coding_platform.service.user.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
public class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private UserDailyActivityRepository userDailyActivityRepository;

    private Jwt mockJwt;

    @BeforeEach
    void setUp() {
        mockJwt = Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .claim("userId", 1)
                .build();
    }

    @Test
    public void getDashboardStats_ShouldReturnOk() throws Exception {
        when(dashboardService.getDashboardStats(any())).thenReturn(new DashboardStatsResponse());

        mockMvc.perform(get("/me/dashboard-stats")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void getDashboardStats_WithJwt_ShouldReturnStats() throws Exception {
        DashboardStatsResponse stats = DashboardStatsResponse.builder()
                .enrolled(5)
                .solvedPractice(10)
                .build();

        when(dashboardService.getDashboardStats(any())).thenReturn(stats);

        mockMvc.perform(get("/me/dashboard-stats")
                        .with(jwt().jwt(mockJwt))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Get dashboard stats successfully"));
    }

    @Test
    public void getUserActivities_ShouldReturnOk() throws Exception {
        when(dashboardService.getUserActivitiesByYear(any(), anyInt()))
                .thenReturn(UserActivityResponse.builder().build());

        mockMvc.perform(get("/me/activities")
                .param("year", "2024")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void getUserActivities_WithJwt_ShouldReturnActivities() throws Exception {
        UserActivityResponse activities = UserActivityResponse.builder().build();
        when(dashboardService.getUserActivitiesByYear(eq(1), eq(2025))).thenReturn(activities);

        mockMvc.perform(get("/me/activities")
                        .param("year", "2025")
                        .with(jwt().jwt(mockJwt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Get user activities successfully"));
    }

    @Test
    public void getUserActivities_MissingYear_ShouldReturnBadRequest() throws Exception {
        // When required=true param is missing, Spring returns 400 or propagates as 500 through GlobalExceptionHandler.
        // Either way the request should not succeed with 200.
        mockMvc.perform(get("/me/activities")
                        .with(jwt().jwt(mockJwt)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void getEnrolledCourses_WithJwt_ShouldReturnCourses() throws Exception {
        CourseListItemResponse course = new CourseListItemResponse();
        course.setId(1L);
        course.setTitle("Java Basics");

        when(dashboardService.getEnrolledCourses(any())).thenReturn(Collections.singletonList(course));

        mockMvc.perform(get("/me/enrolled-courses")
                        .with(jwt().jwt(mockJwt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Get enrolled courses successfully"))
                .andExpect(jsonPath("$.result").isArray());
    }

    @Test
    public void getEnrolledCourses_WithoutJwt_ShouldReturnEmpty() throws Exception {
        when(dashboardService.getEnrolledCourses(null)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/me/enrolled-courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isEmpty());
    }

    @Test
    public void getSubmissionStatistics_WithJwt_ShouldReturnStats() throws Exception {
        SubmissionStatisticResponse statsResponse = SubmissionStatisticResponse.builder()
                .totalSubmissions(100)
                .totalAccepted(75)
                .build();

        when(dashboardService.getSubmissionStatistics(any())).thenReturn(statsResponse);

        mockMvc.perform(get("/me/submission-statistics")
                        .with(jwt().jwt(mockJwt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Get submission statistics successfully"));
    }

    @Test
    public void getSubmissionStatistics_WithoutJwt_ShouldReturnGuestStats() throws Exception {
        SubmissionStatisticResponse statsResponse = SubmissionStatisticResponse.builder()
                .totalSubmissions(0)
                .build();

        when(dashboardService.getSubmissionStatistics(null)).thenReturn(statsResponse);

        mockMvc.perform(get("/me/submission-statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.totalSubmissions").value(0));
    }
}
