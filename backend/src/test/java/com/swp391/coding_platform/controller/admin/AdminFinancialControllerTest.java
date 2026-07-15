package com.swp391.coding_platform.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swp391.coding_platform.dto.response.AdminFinancialDetailsResponse;
import com.swp391.coding_platform.dto.response.AdminFinancialMonthlyRecordResponse;
import com.swp391.coding_platform.dto.response.AdminFinancialPayoutDetailsResponse;
import com.swp391.coding_platform.dto.response.AdminFinancialTopCourseResponse;
import com.swp391.coding_platform.dto.response.PageResponse;
import com.swp391.coding_platform.service.admin.AdminFinancialService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = AdminFinancialController.class)
public class AdminFinancialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminFinancialService adminFinancialService;

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private com.swp391.coding_platform.repository.user.UserDailyActivityRepository userDailyActivityRepository;

    // --- /monthly-records ---

    @Test
    public void getMonthlyRecords_ShouldReturnOk() throws Exception {
        when(adminFinancialService.getMonthlyFinancialRecords()).thenReturn(List.of());

        mockMvc.perform(get("/admin/financial/monthly-records")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void getMonthlyRecords_ShouldReturnData() throws Exception {
        AdminFinancialMonthlyRecordResponse record = new AdminFinancialMonthlyRecordResponse();
        when(adminFinancialService.getMonthlyFinancialRecords()).thenReturn(List.of(record));

        mockMvc.perform(get("/admin/financial/monthly-records")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Fetched admin financial monthly records successfully"))
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result.length()").value(1));
    }

    // --- /top-courses ---

    @Test
    public void getTopCourses_ShouldReturnOk() throws Exception {
        when(adminFinancialService.getTopRevenueCoursesData()).thenReturn(List.of());

        mockMvc.perform(get("/admin/financial/top-courses")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void getTopCourses_ShouldReturnData() throws Exception {
        AdminFinancialTopCourseResponse course = new AdminFinancialTopCourseResponse();
        when(adminFinancialService.getTopRevenueCoursesData()).thenReturn(List.of(course));

        mockMvc.perform(get("/admin/financial/top-courses")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Fetched admin top revenue courses successfully"))
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result.length()").value(1));
    }

    // --- /details ---

    @Test
    public void getFinancialDetails_ShouldReturnOk() throws Exception {
        AdminFinancialDetailsResponse details = new AdminFinancialDetailsResponse();
        when(adminFinancialService.getFinancialDetails()).thenReturn(details);

        mockMvc.perform(get("/admin/financial/details")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Fetched admin financial audit details successfully"));
    }

    // --- /orders ---

    @Test
    public void getOrdersPage_DefaultParams_ShouldReturnOk() throws Exception {
        PageResponse<AdminFinancialDetailsResponse.OrderDetails> page = PageResponse.<AdminFinancialDetailsResponse.OrderDetails>builder()
                .content(List.of())
                .totalElements(0)
                .totalPages(0)
                .build();
        when(adminFinancialService.getOrdersPage(eq(1), eq(10), isNull(), isNull())).thenReturn(page);

        mockMvc.perform(get("/admin/financial/orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Fetched orders successfully"));
    }

    @Test
    public void getOrdersPage_WithDateFilters_ShouldReturnOk() throws Exception {
        PageResponse<AdminFinancialDetailsResponse.OrderDetails> page = PageResponse.<AdminFinancialDetailsResponse.OrderDetails>builder()
                .content(List.of())
                .totalElements(0)
                .totalPages(0)
                .build();
        when(adminFinancialService.getOrdersPage(eq(2), eq(5), eq("2025-01-01"), eq("2025-12-31"))).thenReturn(page);

        mockMvc.perform(get("/admin/financial/orders")
                        .param("page", "2")
                        .param("limit", "5")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-12-31")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // --- /awards ---

    @Test
    public void getAwardsPage_DefaultParams_ShouldReturnOk() throws Exception {
        PageResponse<AdminFinancialDetailsResponse.AwardDetails> page = PageResponse.<AdminFinancialDetailsResponse.AwardDetails>builder()
                .content(List.of())
                .totalElements(0)
                .totalPages(0)
                .build();
        when(adminFinancialService.getAwardsPage(eq(1), eq(10), isNull(), isNull())).thenReturn(page);

        mockMvc.perform(get("/admin/financial/awards")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Fetched awards successfully"));
    }

    @Test
    public void getAwardsPage_WithDateFilters_ShouldReturnOk() throws Exception {
        PageResponse<AdminFinancialDetailsResponse.AwardDetails> page = PageResponse.<AdminFinancialDetailsResponse.AwardDetails>builder()
                .content(List.of())
                .totalElements(0)
                .totalPages(0)
                .build();
        when(adminFinancialService.getAwardsPage(anyInt(), anyInt(), anyString(), anyString())).thenReturn(page);

        mockMvc.perform(get("/admin/financial/awards")
                        .param("page", "1")
                        .param("limit", "10")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-06-30")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // --- /sales ---

    @Test
    public void getSalesPage_DefaultParams_ShouldReturnOk() throws Exception {
        PageResponse<AdminFinancialDetailsResponse.SaleDetails> page = PageResponse.<AdminFinancialDetailsResponse.SaleDetails>builder()
                .content(List.of())
                .totalElements(0)
                .totalPages(0)
                .build();
        when(adminFinancialService.getSalesPage(eq(1), eq(10), isNull(), isNull())).thenReturn(page);

        mockMvc.perform(get("/admin/financial/sales")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Fetched sales successfully"));
    }

    @Test
    public void getSalesPage_WithDateFilters_ShouldReturnOk() throws Exception {
        PageResponse<AdminFinancialDetailsResponse.SaleDetails> page = PageResponse.<AdminFinancialDetailsResponse.SaleDetails>builder()
                .content(List.of())
                .totalElements(0)
                .totalPages(0)
                .build();
        when(adminFinancialService.getSalesPage(anyInt(), anyInt(), anyString(), anyString())).thenReturn(page);

        mockMvc.perform(get("/admin/financial/sales")
                        .param("page", "1")
                        .param("limit", "20")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-12-31")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // --- /payouts ---

    @Test
    public void getPayoutsPage_DefaultParams_ShouldReturnOk() throws Exception {
        PageResponse<AdminFinancialPayoutDetailsResponse> page = PageResponse.<AdminFinancialPayoutDetailsResponse>builder()
                .content(List.of())
                .totalElements(0)
                .totalPages(0)
                .build();
        when(adminFinancialService.getPayoutsPage(eq(1), eq(10), isNull(), isNull())).thenReturn(page);

        mockMvc.perform(get("/admin/financial/payouts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Fetched payouts successfully"));
    }

    @Test
    public void getPayoutsPage_WithDateFilters_ShouldReturnOk() throws Exception {
        PageResponse<AdminFinancialPayoutDetailsResponse> page = PageResponse.<AdminFinancialPayoutDetailsResponse>builder()
                .content(List.of())
                .totalElements(0)
                .totalPages(0)
                .build();
        when(adminFinancialService.getPayoutsPage(anyInt(), anyInt(), anyString(), anyString())).thenReturn(page);

        mockMvc.perform(get("/admin/financial/payouts")
                        .param("page", "2")
                        .param("limit", "5")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-03-31")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
