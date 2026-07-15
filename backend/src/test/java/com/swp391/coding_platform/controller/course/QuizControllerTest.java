package com.swp391.coding_platform.controller.course;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swp391.coding_platform.dto.request.QuizSubmitRequest;
import com.swp391.coding_platform.dto.response.QuizDetailResponse;
import com.swp391.coding_platform.dto.response.QuizSubmitResultResponse;
import com.swp391.coding_platform.service.course.QuizService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = QuizController.class)

public class QuizControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private QuizService quizService;

    @MockBean
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @MockBean
    private com.swp391.coding_platform.repository.user.UserDailyActivityRepository userDailyActivityRepository;

    @Test
    void testGetQuizByLesson_Success() throws Exception {
        QuizDetailResponse mockResult = new QuizDetailResponse();
        
        when(quizService.getQuizDetailByLessonId(10, 100)).thenReturn(mockResult);

        mockMvc.perform(get("/courses/1/lessons/10/quiz")
                .with(jwt().jwt(j -> j.claim("userId", 100))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Get quiz details successfully"));
    }

    @Test
    void testGetQuizByLesson_NoJwt() throws Exception {
        mockMvc.perform(get("/courses/1/lessons/10/quiz"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testSubmitQuiz_Success() throws Exception {
        QuizSubmitRequest request = new QuizSubmitRequest();
        request.setAnswers(java.util.List.of());
        
        QuizSubmitResultResponse mockResult = new QuizSubmitResultResponse();

        when(quizService.submitQuiz(eq(20), eq(100), any(QuizSubmitRequest.class))).thenReturn(mockResult);

        mockMvc.perform(post("/courses/1/quizzes/20/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(jwt().jwt(j -> j.claim("userId", 100)))
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Submit quiz successfully"));
    }

    @Test
    void testSubmitQuiz_NoJwt() throws Exception {
        QuizSubmitRequest request = new QuizSubmitRequest();
        
        mockMvc.perform(post("/courses/1/quizzes/20/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isUnauthorized());
    }
}
