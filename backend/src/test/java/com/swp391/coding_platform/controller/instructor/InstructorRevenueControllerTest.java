package com.swp391.coding_platform.controller.instructor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swp391.coding_platform.dto.response.*;
import com.swp391.coding_platform.service.instructor.InstructorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InstructorRevenueController.class)

public class InstructorRevenueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InstructorService instructorService;

    @MockBean
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @MockBean
    private com.swp391.coding_platform.repository.user.UserDailyActivityRepository userDailyActivityRepository;

    @Test
    void getRevenueSummary_ReturnsSummary() throws Exception {
        InstructorRevenueSummary summary = new InstructorRevenueSummary();
        when(instructorService.getRevenueSummary(eq(1), eq("this-month"), isNull(), isNull())).thenReturn(summary);

        mockMvc.perform(get("/instructor/revenue/summary")
                        .param("filter", "this-month")
                        .with(jwt().jwt(builder -> builder.claim("userId", 1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Fetched revenue summary successfully"));
    }

    @Test
    void getSalesHistory_ReturnsHistory() throws Exception {
        SalesHistoryItem item = new SalesHistoryItem();
        when(instructorService.getSalesHistory(eq(1), eq("this-month"), isNull(), isNull())).thenReturn(List.of(item));

        mockMvc.perform(get("/instructor/revenue/sales-history")
                        .param("filter", "this-month")
                        .with(jwt().jwt(builder -> builder.claim("userId", 1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Fetched sales history successfully"));
    }

    @Test
    void getRecentRegistrations_ReturnsRegistrations() throws Exception {
        RecentRegistration reg = new RecentRegistration();
        when(instructorService.getRecentRegistrations(1)).thenReturn(List.of(reg));

        mockMvc.perform(get("/instructor/revenue/recent-registrations")
                        .with(jwt().jwt(builder -> builder.claim("userId", 1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Fetched recent registrations successfully"));
    }

    @Test
    void getPayoutHistory_ReturnsHistory() throws Exception {
        PayoutHistoryItem payout = new PayoutHistoryItem();
        when(instructorService.getPayoutHistory(1)).thenReturn(List.of(payout));

        mockMvc.perform(get("/instructor/revenue/payout-history")
                        .with(jwt().jwt(builder -> builder.claim("userId", 1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Fetched payout history successfully"));
    }

    @Test
    void getCourseBreakdown_ReturnsBreakdown() throws Exception {
        CourseBreakdownItem breakdown = new CourseBreakdownItem();
        when(instructorService.getCourseBreakdown(eq(1), eq("this-month"), isNull(), isNull())).thenReturn(List.of(breakdown));

        mockMvc.perform(get("/instructor/revenue/course-breakdown")
                        .param("filter", "this-month")
                        .with(jwt().jwt(builder -> builder.claim("userId", 1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Fetched course revenue breakdown successfully"));
    }

    @Test
    void getMonthlyChartData_ReturnsChartData() throws Exception {
        MonthlyChartItem chartItem = new MonthlyChartItem();
        when(instructorService.getMonthlyChartData(1)).thenReturn(List.of(chartItem));

        mockMvc.perform(get("/instructor/revenue/chart-data")
                        .with(jwt().jwt(builder -> builder.claim("userId", 1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Fetched monthly chart data successfully"));
    }

    @Test
    void getCourseRegistrations_ReturnsCourseRegistrations() throws Exception {
        InstructorCourseRegistrationsResponse response = new InstructorCourseRegistrationsResponse();
        when(instructorService.getCourseRegistrations(1, "12m")).thenReturn(response);

        mockMvc.perform(get("/instructor/revenue/course-registrations")
                        .param("trendTimeframe", "12m")
                        .with(jwt().jwt(builder -> builder.claim("userId", 1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Fetched course registrations stats successfully"));
    }
}
