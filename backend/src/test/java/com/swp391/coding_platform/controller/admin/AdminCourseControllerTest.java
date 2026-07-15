package com.swp391.coding_platform.controller.admin;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import com.swp391.coding_platform.repository.user.UserDailyActivityRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swp391.coding_platform.entity.course.CourseEntity;
import com.swp391.coding_platform.entity.enums.CourseStatus;
import com.swp391.coding_platform.repository.course.ChapterRepository;
import com.swp391.coding_platform.repository.course.CourseRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AdminCourseController.class)

@AutoConfigureMockMvc(addFilters = false)
class AdminCourseControllerTest {

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private UserDailyActivityRepository userDailyActivityRepository;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseRepository courseRepository;

    @MockBean
    private ChapterRepository chapterRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getCourses_Success() throws Exception {
        CourseEntity course = new CourseEntity();
        course.setId(1L);
        course.setTitle("Test Course");
        course.setStatus(CourseStatus.APPROVED);
        course.setPrice(BigDecimal.TEN);
        course.setAverageRating(4.5);

        when(courseRepository.findAll()).thenReturn(List.of(course));
        when(chapterRepository.countByCourseId(1L)).thenReturn(5);

        mockMvc.perform(get("/admin/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Fetched admin courses successfully"))
                .andExpect(jsonPath("$.result[0].title").value("Test Course"))
                .andExpect(jsonPath("$.result[0].totalChapters").value(5));
    }

    @Test
    void approveCourse_Success() throws Exception {
        CourseEntity course = new CourseEntity();
        course.setId(1L);
        course.setTitle("Test Course");
        course.setStatus(CourseStatus.PENDING_ADMIN);
        course.setPrice(BigDecimal.TEN);
        course.setAverageRating(0.0);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(chapterRepository.countByCourseId(1L)).thenReturn(0);

        String requestBody = "{\"status\":\"APPROVED\", \"adminNote\":\"Looks good\"}";

        mockMvc.perform(post("/admin/courses/1/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.message").value("Course approved successfully"))
                .andExpect(jsonPath("$.result.status").value("APPROVED"));
    }

    @Test
    void approveCourse_InvalidStatus_ReturnsBadRequest() throws Exception {
        CourseEntity course = new CourseEntity();
        course.setId(1L);
        course.setStatus(CourseStatus.PENDING_ADMIN);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        String requestBody = "{\"status\":\"INVALID_STATUS\", \"adminNote\":\"Looks good\"}";

        mockMvc.perform(post("/admin/courses/1/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(4000));
    }
}
