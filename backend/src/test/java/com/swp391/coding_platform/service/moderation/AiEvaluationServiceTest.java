package com.swp391.coding_platform.service.moderation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swp391.coding_platform.dto.moderation.CourseModerationPayload;
import com.swp391.coding_platform.dto.moderation.GeminiResponse;
import com.swp391.coding_platform.dto.moderation.ModerationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiEvaluationServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private ObjectMapper objectMapper;
    private AiEvaluationService aiEvaluationService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        aiEvaluationService = new AiEvaluationService(webClient, objectMapper);
    }

    @Test
    void testEvaluateCourse_MissingApiKey_ReturnsMock() {
        ReflectionTestUtils.setField(aiEvaluationService, "geminiApiKey", "");
        CourseModerationPayload payload = new CourseModerationPayload();

        ModerationResult result = aiEvaluationService.evaluateCourse(payload);

        assertTrue(result.getIsClean());
        assertTrue(result.getCourseViolations().isEmpty());
        assertTrue(result.getLessonViolations().isEmpty());
    }

    @Test
    void testEvaluateSingleLesson_MissingApiKey_ReturnsMock() {
        ReflectionTestUtils.setField(aiEvaluationService, "geminiApiKey", null);
        CourseModerationPayload.LessonPayload payload = new CourseModerationPayload.LessonPayload();

        ModerationResult result = aiEvaluationService.evaluateSingleLesson(payload);

        assertTrue(result.getIsClean());
        assertTrue(result.getLessonViolations().isEmpty());
    }

    @Test
    void evaluateCourse_SuccessWithApiKey() {
        ReflectionTestUtils.setField(aiEvaluationService, "geminiApiKey", "test-api-key");
        ReflectionTestUtils.setField(aiEvaluationService, "geminiModel", "gemini-1.5-flash");

        CourseModerationPayload payload = new CourseModerationPayload();

        GeminiResponse.Part part = new GeminiResponse.Part("{\"isClean\":true,\"courseViolations\":[],\"lessonViolations\":[]}");
        GeminiResponse.Content content = new GeminiResponse.Content(List.of(part));
        GeminiResponse.Candidate candidate = new GeminiResponse.Candidate(content);
        GeminiResponse geminiResponse = new GeminiResponse(List.of(candidate));

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(java.net.URI.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(GeminiResponse.class)).thenReturn(Mono.just(geminiResponse));

        ModerationResult result = aiEvaluationService.evaluateCourse(payload);

        assertNotNull(result);
        assertTrue(result.getIsClean());
        assertTrue(result.getCourseViolations().isEmpty());
    }

    @Test
    void evaluateCourse_WebClientError_ReturnsSystemError() {
        ReflectionTestUtils.setField(aiEvaluationService, "geminiApiKey", "test-api-key");
        ReflectionTestUtils.setField(aiEvaluationService, "geminiModel", "gemini-1.5-flash");

        CourseModerationPayload payload = new CourseModerationPayload();

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(java.net.URI.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        // Throw 400 Bad Request directly to bypass retry sleep logic for 503/429
        when(responseSpec.bodyToMono(GeminiResponse.class))
                .thenReturn(Mono.error(new WebClientResponseException(400, "Bad Request", null, null, null)));

        ModerationResult result = aiEvaluationService.evaluateCourse(payload);

        assertNotNull(result);
        assertFalse(result.getIsClean());
        assertEquals("Hệ thống kiểm duyệt AI gặp lỗi kết nối hoặc phân tích JSON.", result.getCourseViolations().get(0));
    }

    @Test
    void evaluateCourse_JsonParseError_ReturnsSystemError() {
        ReflectionTestUtils.setField(aiEvaluationService, "geminiApiKey", "test-api-key");
        ReflectionTestUtils.setField(aiEvaluationService, "geminiModel", "gemini-1.5-flash");

        CourseModerationPayload payload = new CourseModerationPayload();

        // Gemini returns invalid JSON content
        GeminiResponse.Part part = new GeminiResponse.Part("invalid json text");
        GeminiResponse.Content content = new GeminiResponse.Content(List.of(part));
        GeminiResponse.Candidate candidate = new GeminiResponse.Candidate(content);
        GeminiResponse geminiResponse = new GeminiResponse(List.of(candidate));

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(java.net.URI.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(GeminiResponse.class)).thenReturn(Mono.just(geminiResponse));

        ModerationResult result = aiEvaluationService.evaluateCourse(payload);

        assertNotNull(result);
        assertFalse(result.getIsClean());
        assertEquals("Hệ thống kiểm duyệt AI gặp lỗi kết nối hoặc phân tích JSON.", result.getCourseViolations().get(0));
    }
}
