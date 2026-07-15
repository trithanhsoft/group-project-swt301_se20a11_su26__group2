package com.swp391.coding_platform.controller.problem;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swp391.coding_platform.dto.request.CreateCommentRequest;
import com.swp391.coding_platform.dto.response.*;
import com.swp391.coding_platform.repository.user.UserDailyActivityRepository;
import com.swp391.coding_platform.service.problem.ProblemCommentService;
import com.swp391.coding_platform.service.problem.ProblemSubmissionService;
import com.swp391.coding_platform.service.problem.UserProblemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProblemController.class)
class ProblemControllerTest {

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private UserDailyActivityRepository userDailyActivityRepository;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserProblemService userProblemService;

    @MockBean
    private ProblemSubmissionService problemSubmissionService;

    @MockBean
    private ProblemCommentService problemCommentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getProblems_shouldReturnOk() throws Exception {
        ProblemListItemResponse problem = ProblemListItemResponse.builder()
                .id(1)
                .title("Two Sum")
                .difficulty("Easy")
                .score(10)
                .isSolved(true)
                .build();

        when(userProblemService.getProblems(any())).thenReturn(Collections.singletonList(problem));

        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").claim("userId", 1).build();

        mockMvc.perform(get("/api/problems")
                        .with(jwt().jwt(jwt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result[0].id").value(1))
                .andExpect(jsonPath("$.result[0].title").value("Two Sum"));
    }

    @Test
    void getProblemDescription_shouldReturnOk() throws Exception {
        ProblemDescriptionResponse descriptionResponse = ProblemDescriptionResponse.builder()
                .id(1)
                .title("Two Sum")
                .description("<p>Solve Two Sum</p>")
                .build();

        when(userProblemService.getProblemDescription(eq(1), any())).thenReturn(descriptionResponse);

        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").claim("userId", 1).build();

        mockMvc.perform(get("/api/problems/1/description")
                        .with(jwt().jwt(jwt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.id").value(1))
                .andExpect(jsonPath("$.result.description").value("<p>Solve Two Sum</p>"));
    }

    @Test
    void getDiscussion_shouldReturnOk() throws Exception {
        ProblemCommentResponse commentResponse = ProblemCommentResponse.builder()
                .id(10)
                .text("Nice work!")
                .author("Coder")
                .build();

        when(problemCommentService.getComments(1)).thenReturn(List.of(commentResponse));

        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").claim("userId", 1).build();

        mockMvc.perform(get("/api/problems/1/discussion")
                        .with(jwt().jwt(jwt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result[0].id").value(10))
                .andExpect(jsonPath("$.result[0].text").value("Nice work!"));
    }

    @Test
    void addDiscussionComment_shouldReturnOk() throws Exception {
        CreateCommentRequest request = new CreateCommentRequest("Excellent task!", null);
        ProblemCommentResponse commentResponse = ProblemCommentResponse.builder()
                .id(20)
                .text("Excellent task!")
                .author("Student")
                .build();

        when(problemCommentService.addComment(eq(1), eq(1), any(CreateCommentRequest.class))).thenReturn(commentResponse);

        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").claim("userId", 1).build();

        mockMvc.perform(post("/api/problems/1/discussion")
                        .with(jwt().jwt(jwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.id").value(20))
                .andExpect(jsonPath("$.result.text").value("Excellent task!"));
    }

    @Test
    void getProblemSolution_shouldReturnOk() throws Exception {
        ProblemSolutionResponse solutionResponse = ProblemSolutionResponse.builder()
                .problemId(1)
                .solutionCode("<pre>Java code</pre>")
                .build();

        when(userProblemService.getProblemSolution(eq(1), any())).thenReturn(solutionResponse);

        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").claim("userId", 1).build();

        mockMvc.perform(get("/api/problems/1/solution")
                        .with(jwt().jwt(jwt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.problemId").value(1))
                .andExpect(jsonPath("$.result.solutionCode").value("<pre>Java code</pre>"));
    }

    @Test
    void getSubmissions_shouldReturnOk() throws Exception {
        ProblemSubmissionResponse submission = ProblemSubmissionResponse.builder()
                .problemId(100)
                .status("ACCEPTED")
                .build();

        when(problemSubmissionService.getSubmissions(eq(1), any())).thenReturn(List.of(submission));

        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").claim("userId", 1).build();

        mockMvc.perform(get("/api/problems/1/submissions")
                        .with(jwt().jwt(jwt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result[0].problemId").value(100))
                .andExpect(jsonPath("$.result[0].status").value("ACCEPTED"));
    }
}
