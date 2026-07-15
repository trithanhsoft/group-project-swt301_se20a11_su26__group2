package com.swp391.coding_platform.controller.judge0;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import com.swp391.coding_platform.repository.user.UserDailyActivityRepository;

import com.swp391.coding_platform.dto.judge0.Judge0CallbackPayload;
import com.swp391.coding_platform.dto.judge0.Judge0CallbackPayload.Judge0Status;
import com.swp391.coding_platform.dto.request.OjSubmissionRequest;
import com.swp391.coding_platform.dto.response.OjSubmissionInitialResponse;
import com.swp391.coding_platform.service.judge0.Judge0Service;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = Judge0Controller.class)
class Judge0ControllerTest {

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private UserDailyActivityRepository userDailyActivityRepository;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Judge0Service judge0Service;

    @Test
    void submitCode_shouldReturnOk() throws Exception {
        OjSubmissionInitialResponse initialResponse = OjSubmissionInitialResponse.builder()
                .submissionId(10)
                .status("PENDING")
                .message("Processing")
                .build();

        when(judge0Service.submitCode(any(OjSubmissionRequest.class), any())).thenReturn(initialResponse);

        String requestJson = """
                {
                    "problemId": 1,
                    "languageId": 71,
                    "sourceCode": "print('Hello')"
                }
                """;

        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").claim("userId", 1).build();

        mockMvc.perform(post("/online-judge/submissions")
                        .with(jwt().jwt(jwt))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.submissionId").value(10));
    }

    @Test
    void processJudge0Callback_shouldReturnOk() throws Exception {
        doNothing().when(judge0Service).processJudge0Callback(any(Judge0CallbackPayload.class));

        String callbackJson = """
                {
                    "token": "tok123",
                    "status": {
                        "id": 3,
                        "description": "Accepted"
                    },
                    "time": "0.012",
                    "memory": 1234
                }
                """;

        mockMvc.perform(put("/online-judge/submissions")
                        .with(jwt())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(callbackJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000));
    }
}
