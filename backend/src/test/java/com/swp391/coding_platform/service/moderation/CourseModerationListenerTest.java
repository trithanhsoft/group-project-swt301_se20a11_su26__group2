package com.swp391.coding_platform.service.moderation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseModerationListenerTest {

    @Mock
    private CourseModerationService moderationService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CourseModerationListener listener;

    @Test
    void processCourseModeration_WithLongMessage_ProcessesFullCourse() {
        Long courseId = 123L;
        listener.processCourseModeration(courseId);
        verify(moderationService, times(1)).processFullCourse(courseId);
    }

    @Test
    void processCourseModeration_WithMapMessageFullCourse_ProcessesFullCourse() {
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("type", "FULL_COURSE");
        messageMap.put("courseId", 123L);

        listener.processCourseModeration(messageMap);
        verify(moderationService, times(1)).processFullCourse(123L);
    }

    @Test
    void processCourseModeration_WithMapMessageSingleLesson_ProcessesSingleLesson() {
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("type", "SINGLE_LESSON");
        messageMap.put("lessonId", 456L);

        listener.processCourseModeration(messageMap);
        verify(moderationService, times(1)).processSingleLessonUpdate(456L);
    }

    @Test
    void processCourseModeration_WithAmqpMessageFullCourse_ProcessesFullCourse() throws Exception {
        String jsonBody = "{\"type\":\"FULL_COURSE\",\"courseId\":123}";
        Message message = mock(Message.class);
        when(message.getBody()).thenReturn(jsonBody.getBytes());

        Map<String, Object> map = new HashMap<>();
        map.put("type", "FULL_COURSE");
        map.put("courseId", 123);

        when(objectMapper.readValue(eq(jsonBody), any(TypeReference.class))).thenReturn(map);

        listener.processCourseModeration(message);

        verify(moderationService, times(1)).processFullCourse(123L);
    }

    @Test
    void processCourseModeration_ExceptionThrown_DoesNotThrowOut() {
        Long courseId = 123L;
        doThrow(new RuntimeException("Test Exception")).when(moderationService).processFullCourse(courseId);
        
        listener.processCourseModeration(courseId);
        verify(moderationService, times(1)).processFullCourse(courseId);
    }
}
