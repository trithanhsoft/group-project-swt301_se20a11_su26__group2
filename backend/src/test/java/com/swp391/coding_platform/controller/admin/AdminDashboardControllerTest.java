package com.swp391.coding_platform.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swp391.coding_platform.dto.response.AdminDashboardStatsResponse;
import com.swp391.coding_platform.service.admin.AdminDashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = AdminDashboardController.class)

public class AdminDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminDashboardService adminDashboardService;

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private com.swp391.coding_platform.repository.user.UserDailyActivityRepository userDailyActivityRepository;

    @Test
    public void getDashboardStats_ShouldReturnOk() throws Exception {
        when(adminDashboardService.getDashboardStats()).thenReturn(new AdminDashboardStatsResponse());

        mockMvc.perform(get("/admin/dashboard/stats")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void getRecentDeposits_ShouldReturnOk() throws Exception {
        when(adminDashboardService.getRecentDeposits()).thenReturn(List.of());

        mockMvc.perform(get("/admin/dashboard/recent-deposits")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
