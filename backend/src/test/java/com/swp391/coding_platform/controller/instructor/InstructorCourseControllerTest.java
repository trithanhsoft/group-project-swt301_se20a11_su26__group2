package com.swp391.coding_platform.controller.instructor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swp391.coding_platform.dto.request.InstructorCourseCreateRequest;
import com.swp391.coding_platform.dto.request.InstructorCourseUpdateRequest;
import com.swp391.coding_platform.dto.request.TestcaseGeneratorRequest;
import com.swp391.coding_platform.dto.response.CloudinaryResponse;
import com.swp391.coding_platform.dto.response.CourseStatisticResponse;
import com.swp391.coding_platform.dto.response.InstructorCourseDetailResponse;
import com.swp391.coding_platform.dto.response.InstructorCourseResponse;
import com.swp391.coding_platform.service.cloudinary.CloudinaryService;
import com.swp391.coding_platform.service.instructor.InstructorCourseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = InstructorCourseController.class)

public class InstructorCourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InstructorCourseService instructorCourseService;

    @MockBean
    private CloudinaryService cloudinaryService;

    @MockBean
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @MockBean
    private com.swp391.coding_platform.repository.user.UserDailyActivityRepository userDailyActivityRepository;

    @Test
    void getCourses_ReturnsCourses_WhenAuthenticated() throws Exception {
        InstructorCourseResponse response = new InstructorCourseResponse();
        when(instructorCourseService.getCourses(1)).thenReturn(List.of(response));

        mockMvc.perform(get("/instructor/courses")
                        .with(jwt().jwt(builder -> builder.claim("userId", 1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Fetched instructor courses successfully"))
                .andExpect(jsonPath("$.result").isArray());
    }

    @Test
    void createCourse_ReturnsCreatedCourse() throws Exception {
        InstructorCourseCreateRequest request = new InstructorCourseCreateRequest();
        InstructorCourseResponse response = new InstructorCourseResponse();
        
        when(instructorCourseService.createCourse(eq(1), any(InstructorCourseCreateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/instructor/courses")
                        .with(jwt().jwt(builder -> builder.claim("userId", 1)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Course created successfully"));
    }

    @Test
    void uploadMedia_ReturnsCloudinaryResponse() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test image".getBytes());
        CloudinaryResponse response = new CloudinaryResponse("url", "public_id");

        when(cloudinaryService.uploadFile(any(), eq("courses"))).thenReturn(response);

        mockMvc.perform(multipart("/instructor/upload")
                        .file(file)
                        .param("folderName", "courses")
                        .with(jwt().jwt(builder -> builder.claim("userId", 1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("File uploaded successfully"))
                .andExpect(jsonPath("$.result.secureUrl").value("url"));
    }

    @Test
    void getCourseDetail_ReturnsCourseDetail() throws Exception {
        InstructorCourseDetailResponse response = new InstructorCourseDetailResponse();
        when(instructorCourseService.getCourseDetail(1, 100L)).thenReturn(response);

        mockMvc.perform(get("/instructor/courses/100")
                        .with(jwt().jwt(builder -> builder.claim("userId", 1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Fetched instructor course detail successfully"));
    }

    @Test
    void updateCourse_ReturnsUpdatedCourse() throws Exception {
        InstructorCourseUpdateRequest request = new InstructorCourseUpdateRequest();
        InstructorCourseResponse response = new InstructorCourseResponse();

        when(instructorCourseService.updateCourse(eq(1), eq(100L), any(InstructorCourseUpdateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/instructor/courses/100")
                        .with(jwt().jwt(builder -> builder.claim("userId", 1)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Course draft saved successfully."));
    }

    @Test
    void generateTestcases_ReturnsTestcases() throws Exception {
        TestcaseGeneratorRequest request = new TestcaseGeneratorRequest();
        request.setLanguage("Sample prompt");
        request.setCode("print('hello')");

        InstructorCourseUpdateRequest.TestcaseDto testcase = new InstructorCourseUpdateRequest.TestcaseDto();
        testcase.setInput("input");
        testcase.setOutput("output");

        when(instructorCourseService.generateTestcases(any(TestcaseGeneratorRequest.class)))
                .thenReturn(List.of(testcase));

        mockMvc.perform(post("/instructor/testcases/generate")
                        .with(jwt().jwt(builder -> builder.claim("userId", 1)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Testcases generated successfully."));
    }

    @Test
    void submitCourseForReview_ReturnsOk() throws Exception {
        mockMvc.perform(put("/instructor/courses/100/submit-review")
                        .with(jwt().jwt(builder -> builder.claim("userId", 1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Course submitted for review successfully"));
    }

    @Test
    void getCourseStatistics_ReturnsStatistics() throws Exception {
        CourseStatisticResponse response = new CourseStatisticResponse();
        when(instructorCourseService.getCourseStatistics(1, 100L)).thenReturn(response);

        mockMvc.perform(get("/instructor/courses/100/statistics")
                        .with(jwt().jwt(builder -> builder.claim("userId", 1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Fetched course statistics successfully"));
    }
}
