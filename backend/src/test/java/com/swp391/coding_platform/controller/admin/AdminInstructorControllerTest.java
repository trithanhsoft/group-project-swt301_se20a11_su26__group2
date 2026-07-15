package com.swp391.coding_platform.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swp391.coding_platform.service.instructor.InstructorApplicationService;
import com.swp391.coding_platform.service.instructor.InstructorService;
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
@WebMvcTest(controllers = AdminInstructorController.class)

public class AdminInstructorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InstructorService instructorService;

    @MockBean
    private InstructorApplicationService applicationService;

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private com.swp391.coding_platform.repository.user.UserDailyActivityRepository userDailyActivityRepository;

    @Test
    public void getInstructors_ShouldReturnOk() throws Exception {
        when(instructorService.getAllInstructorsForAdmin()).thenReturn(List.of());

        mockMvc.perform(get("/admin/instructors")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void getApplications_ShouldReturnOk() throws Exception {
        when(applicationService.getApplications()).thenReturn(List.of());

        mockMvc.perform(get("/admin/instructors/applications")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
