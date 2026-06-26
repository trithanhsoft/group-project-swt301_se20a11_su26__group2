package com.swp391.coding_platform.service.moderation;

import com.swp391.coding_platform.configuration.ModerationQueueConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Message;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseModerationListener {

    private final CourseModerationService moderationService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = ModerationQueueConfig.MODERATION_QUEUE, containerFactory = "moderationContainerFactory")
    public void processCourseModeration(Object message) {
        log.info("Nhận tin nhắn RabbitMQ: {}", message);
        
        try {
            // Để tương thích ngược với code cũ, kiểm tra nếu message là kiểu Long (Course ID cũ)
            if (message instanceof Long || message instanceof Integer) {
                Long courseId = Long.valueOf(message.toString());
                moderationService.processFullCourse(courseId);
            } 
            // Cấu trúc mới hỗ trợ SINGLE_LESSON (nếu Spring convert thành Map)
            else if (message instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) message;
                String type = (String) map.get("type");
                
                if ("FULL_COURSE".equals(type)) {
                    Long courseId = Long.valueOf(map.get("courseId").toString());
                    moderationService.processFullCourse(courseId);
                } else if ("SINGLE_LESSON".equals(type)) {
                    Long lessonId = Long.valueOf(map.get("lessonId").toString());
                    moderationService.processSingleLessonUpdate(lessonId);
                }
            } 
            // Xử lý trực tiếp org.springframework.amqp.core.Message nếu Spring không tự convert
            else if (message instanceof org.springframework.amqp.core.Message) {
                org.springframework.amqp.core.Message amqpMessage = (org.springframework.amqp.core.Message) message;
                String body = new String(amqpMessage.getBody(), java.nio.charset.StandardCharsets.UTF_8);
                
                // Nếu body chỉ là chuỗi số (tương thích cũ)
                if (body.matches("^\"?\\d+\"?$")) {
                    String cleanBody = body.replace("\"", "");
                    moderationService.processFullCourse(Long.valueOf(cleanBody));
                } else {
                    // Parse chuỗi JSON thành Map
                    Map<String, Object> map = objectMapper.readValue(body, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                    String type = (String) map.get("type");
                    
                    if ("FULL_COURSE".equals(type)) {
                        Long courseId = Long.valueOf(map.get("courseId").toString());
                        moderationService.processFullCourse(courseId);
                    } else if ("SINGLE_LESSON".equals(type)) {
                        Long lessonId = Long.valueOf(map.get("lessonId").toString());
                        moderationService.processSingleLessonUpdate(lessonId);
                    }
                }
            } else {
                log.warn("Định dạng tin nhắn RabbitMQ không được hỗ trợ: {}", message.getClass());
            }
        } catch (Exception e) {
            log.error("Lỗi khi xử lý tin nhắn RabbitMQ", e);
        }
    }
}
