package com.swp391.coding_platform.controller.instructor;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import com.swp391.coding_platform.repository.user.UserDailyActivityRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swp391.coding_platform.dto.request.InstructorApplyRequest;
import com.swp391.coding_platform.dto.response.InstructorApplicationResponse;
import com.swp391.coding_platform.service.instructor.InstructorApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InstructorApplicationController.class)
class InstructorApplicationControllerTest {

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private UserDailyActivityRepository userDailyActivityRepository;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InstructorApplicationService applicationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void apply_ShouldReturnSuccessResponse() throws Exception {
        InstructorApplyRequest request = new InstructorApplyRequest();
        request.setFullName("John Doe");
        request.setMajor("Computer Science");
        request.setBio("Hello world");

        InstructorApplicationResponse response = InstructorApplicationResponse.builder()
                .id(1)
                .status("APPROVED")
                .fullName("John Doe")
                .build();

        when(applicationService.apply(eq(1), any(InstructorApplyRequest.class))).thenReturn(response);

        Jwt mockJwt = Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .claim("userId", 1)
                .build();

        mockMvc.perform(post("/instructor-applications/apply")
                        .with(jwt().jwt(mockJwt))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.fullName").value("John Doe"))
                .andExpect(jsonPath("$.result.status").value("APPROVED"));
    }

    @Test
    void getMyApplicationStatus_ShouldReturnStatus() throws Exception {
        InstructorApplicationResponse response = InstructorApplicationResponse.builder()
                .id(1)
                .status("PENDING")
                .fullName("John Doe")
                .build();

        when(applicationService.getMyApplicationStatus(1)).thenReturn(response);

        Jwt mockJwt = Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .claim("userId", 1)
                .build();

        mockMvc.perform(get("/instructor-applications/my-status")
                        .with(jwt().jwt(mockJwt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.status").value("PENDING"));
    }
}
