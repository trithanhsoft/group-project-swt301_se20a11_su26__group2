package com.swp391.coding_platform.controller.course;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swp391.coding_platform.dto.request.CourseReviewRequest;
import com.swp391.coding_platform.dto.request.CourseSearchRequest;
import com.swp391.coding_platform.dto.request.CreateCommentRequest;
import com.swp391.coding_platform.dto.response.*;
import com.swp391.coding_platform.repository.user.UserDailyActivityRepository;
import com.swp391.coding_platform.service.course.CourseService;
import com.swp391.coding_platform.security.CourseSecurity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CourseController.class)
class CourseControllerTest {

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private UserDailyActivityRepository userDailyActivityRepository;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CourseService courseService;

    @MockBean(name = "courseSecurity")
    private CourseSecurity courseSecurity;

    @Autowired
    private ObjectMapper objectMapper;

    private Jwt mockJwt;

    @BeforeEach
    void setUp() {
        mockJwt = Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .claim("userId", 1)
                .build();
        
        lenient().when(courseSecurity.canAccessCourse(any())).thenReturn(true);
        lenient().when(courseSecurity.canAccessLesson(any(Long.class))).thenReturn(true);
        lenient().when(courseSecurity.canAccessLesson(any(Integer.class))).thenReturn(true);
        lenient().when(courseSecurity.canAccessQuiz(any(Long.class))).thenReturn(true);
    }

    @Test
    void getCourseList_ShouldReturnList() throws Exception {
        CourseListItemResponse item = new CourseListItemResponse();
        item.setId(1L);
        item.setTitle("Java Basics");

        PageResponse<CourseListItemResponse> pageResponse = PageResponse.from(new PageImpl<>(List.of(item)));

        when(courseService.getCourseList(any(), any(CourseSearchRequest.class), any(Pageable.class)))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/courses")
                        .with(jwt().jwt(mockJwt))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.content[0].id").value(1))
                .andExpect(jsonPath("$.result.content[0].title").value("Java Basics"));
    }

    @Test
    void getCourseDetail_ShouldReturnDetail() throws Exception {
        CourseDetailResponse detail = new CourseDetailResponse();
        detail.setId(1L);
        detail.setTitle("Advanced Java");

        when(courseService.getCourseDetail(any(), eq(1L))).thenReturn(detail);

        mockMvc.perform(get("/courses/1")
                        .with(jwt().jwt(mockJwt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.title").value("Advanced Java"));
    }

    @Test
    void getCourseCurriculum_ShouldReturnCurriculum() throws Exception {
        CurriculumChapterResponse chapter = new CurriculumChapterResponse();
        chapter.setId(10);
        chapter.setTitle("Chapter 1");

        when(courseService.getCourseCurriculum(any(), eq(1L))).thenReturn(List.of(chapter));

        mockMvc.perform(get("/courses/1/curriculum")
                        .with(jwt().jwt(mockJwt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].title").value("Chapter 1"));
    }

    @Test
    void getCourseReviews_ShouldReturnStats() throws Exception {
        CourseReviewStatsResponse stats = new CourseReviewStatsResponse();
        stats.setAverageRating(4.5);

        when(courseService.getCourseReviews(eq(1L), any(), any(Pageable.class))).thenReturn(stats);

        mockMvc.perform(get("/courses/1/reviews")
                        .with(jwt().jwt(mockJwt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.averageRating").value(4.5));
    }

    @Test
    void upsertCourseReview_ShouldReturnOk() throws Exception {
        CourseReviewRequest req = new CourseReviewRequest();
        req.setStar(5);
        req.setContent("Excellent!");

        mockMvc.perform(post("/courses/1/reviews")
                        .with(jwt().jwt(mockJwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Review submitted successfully"));

        verify(courseService, times(1)).upsertCourseReview(eq(1L), any(), any(CourseReviewRequest.class));
    }

    @Test
    void getCourseLearningDetail_ShouldReturnLearningDetail() throws Exception {
        LearningDetailResponse detail = new LearningDetailResponse();
        detail.setCourseId(100L);

        when(courseService.getCourseLearningDetail(any(), eq(1L))).thenReturn(detail);

        mockMvc.perform(get("/courses/1/learning-detail")
                        .with(jwt().jwt(mockJwt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.courseId").value(100));
    }

    @Test
    void getCourseLearningCurriculum_ShouldReturnCurriculum() throws Exception {
        when(courseService.getCourseLearningCurriculum(any(), eq(1L))).thenReturn(List.of());

        mockMvc.perform(get("/courses/1/learning-curriculum")
                        .with(jwt().jwt(mockJwt)))
                .andExpect(status().isOk());
    }

    @Test
    void getLearningLessonDetail_ShouldReturnLesson() throws Exception {
        LearningLessonResponse res = new LearningLessonResponse();
        res.setId(200);

        when(courseService.getLearningLessonDetail(any(), eq(1L), eq(200))).thenReturn(res);

        mockMvc.perform(get("/courses/1/lessons/200")
                        .with(jwt().jwt(mockJwt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(200));
    }

    @Test
    void getLessonComments_ShouldReturnComments() throws Exception {
        when(courseService.getLessonComments(200)).thenReturn(List.of());

        mockMvc.perform(get("/courses/lessons/200/comments")
                        .with(jwt().jwt(mockJwt)))
                .andExpect(status().isOk());
    }

    @Test
    void addLessonComment_ShouldReturnOk() throws Exception {
        CreateCommentRequest req = new CreateCommentRequest();
        req.setContent("Nice lesson");

        mockMvc.perform(post("/courses/lessons/200/comments")
                        .with(jwt().jwt(mockJwt))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Comment posted successfully"));

        verify(courseService, times(1)).addLessonComment(eq(200), any(), any(CreateCommentRequest.class));
    }

    @Test
    void completeLesson_ShouldReturnOk() throws Exception {
        mockMvc.perform(post("/courses/1/lessons/200/complete")
                        .with(jwt().jwt(mockJwt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Lesson completed successfully"));

        verify(courseService, times(1)).completeLesson(any(), eq(1L), eq(200));
    }
}
