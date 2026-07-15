package com.swp391.coding_platform.controller.moderation;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import com.swp391.coding_platform.repository.user.UserDailyActivityRepository;

import com.swp391.coding_platform.configuration.ModerationQueueConfig;
import com.swp391.coding_platform.entity.course.CourseModerationReportEntity;
import com.swp391.coding_platform.repository.course.CourseModerationReportRepository;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CourseModerationTestController.class)
@AutoConfigureMockMvc(addFilters = false)
class CourseModerationTestControllerTest {

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private UserDailyActivityRepository userDailyActivityRepository;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @MockBean
    private CourseModerationReportRepository reportRepository;

    @Test
    void testTriggerModeration_Success() throws Exception {
        Long courseId = 1L;

        mockMvc.perform(post("/api/moderation/test/{courseId}", courseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Đã gửi yêu cầu duyệt khóa học vào hàng đợi RabbitMQ thành công!"))
                .andExpect(jsonPath("$.courseId").value(1));

        verify(rabbitTemplate, times(1)).convertAndSend(
                ModerationQueueConfig.MODERATION_EXCHANGE,
                ModerationQueueConfig.MODERATION_ROUTING_KEY,
                courseId
        );
    }

    @Test
    void testGetModerationReport_Found() throws Exception {
        Long courseId = 1L;
        CourseModerationReportEntity report = CourseModerationReportEntity.builder()
                .id(100L)
                .courseId(courseId)
                .status("PASSED")
                .build();

        when(reportRepository.findByCourseId(courseId)).thenReturn(Optional.of(report));

        mockMvc.perform(get("/api/moderation/report/{courseId}", courseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.courseId").value(1))
                .andExpect(jsonPath("$.status").value("PASSED"));

        verify(reportRepository, times(1)).findByCourseId(courseId);
    }

    @Test
    void testGetModerationReport_NotFound() throws Exception {
        Long courseId = 1L;

        when(reportRepository.findByCourseId(courseId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/moderation/report/{courseId}", courseId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Chưa có báo cáo kiểm duyệt cho khóa học này."));

        verify(reportRepository, times(1)).findByCourseId(courseId);
    }
}
