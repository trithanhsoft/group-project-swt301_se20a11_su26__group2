package com.swp391.coding_platform.controller.moderation;

import com.swp391.coding_platform.configuration.ModerationQueueConfig;
import com.swp391.coding_platform.entity.course.CourseModerationReportEntity;
import com.swp391.coding_platform.repository.course.CourseModerationReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/moderation")
@RequiredArgsConstructor
@Slf4j
public class CourseModerationTestController {

    private final RabbitTemplate rabbitTemplate;
    private final CourseModerationReportRepository reportRepository;

    // 1. Kích hoạt luồng kiểm duyệt AI thủ công bằng cách gửi tin nhắn sang RabbitMQ
    @PostMapping("/test/{courseId}")
    public ResponseEntity<?> triggerModeration(@PathVariable Long courseId) {
        log.info("Nhận yêu cầu kiểm thử kiểm duyệt AI cho khóa học ID: {}", courseId);
        
        // Gửi sự kiện kiểm duyệt sang Exchange của RabbitMQ
        rabbitTemplate.convertAndSend(
            ModerationQueueConfig.MODERATION_EXCHANGE,
            ModerationQueueConfig.MODERATION_ROUTING_KEY,
            courseId
        );
        
        return ResponseEntity.ok(Map.of(
            "message", "Đã gửi yêu cầu duyệt khóa học vào hàng đợi RabbitMQ thành công!",
            "courseId", courseId
        ));
    }

    // 2. Lấy kết quả kiểm duyệt chi tiết của khóa học
    @GetMapping("/report/{courseId}")
    public ResponseEntity<?> getModerationReport(@PathVariable Long courseId) {
        log.info("Lấy báo cáo kiểm duyệt AI cho khóa học ID: {}", courseId);
        
        CourseModerationReportEntity report = reportRepository.findByCourseId(courseId).orElse(null);
        if (report == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Chưa có báo cáo kiểm duyệt cho khóa học này."));
        }
        
        return ResponseEntity.ok(report);
    }
}
