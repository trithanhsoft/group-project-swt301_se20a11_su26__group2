package com.swp391.coding_platform.controller.user;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import com.swp391.coding_platform.repository.user.UserDailyActivityRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swp391.coding_platform.dto.request.LockUserRequest;
import com.swp391.coding_platform.dto.response.AdminUserResponse;
import com.swp391.coding_platform.service.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AdminUserController.class)

@AutoConfigureMockMvc(addFilters = false)
class AdminUserControllerTest {

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private UserDailyActivityRepository userDailyActivityRepository;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllUsers_Success() throws Exception {
        AdminUserResponse userResponse = AdminUserResponse.builder()
                .id(1)
                .name("testuser")
                .email("test@test.com")
                .status("ACTIVE")
                .build();

        when(userService.getAllUsersForAdmin()).thenReturn(List.of(userResponse));

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Fetched all users successfully"))
                .andExpect(jsonPath("$.result[0].name").value("testuser"));
    }

    @Test
    void lockOrUnlockUser_Success() throws Exception {
        LockUserRequest request = new LockUserRequest();
        request.setStatus("LOCKED");
        request.setReason("Spam");

        AdminUserResponse response = AdminUserResponse.builder()
                .id(1)
                .name("testuser")
                .status("LOCKED")
                .lockReason("Spam")
                .build();

        when(userService.setUserLockStatus(eq(1), any(LockUserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/admin/users/1/lock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("User status updated successfully"))
                .andExpect(jsonPath("$.result.status").value("LOCKED"));
    }
}
