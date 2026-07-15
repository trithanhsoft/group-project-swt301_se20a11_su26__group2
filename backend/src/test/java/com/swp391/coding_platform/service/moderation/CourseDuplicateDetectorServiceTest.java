package com.swp391.coding_platform.service.moderation;

import com.swp391.coding_platform.dto.moderation.GeminiEmbeddingResponse;
import com.swp391.coding_platform.repository.course.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseDuplicateDetectorServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private CourseDuplicateDetectorService service;

    @BeforeEach
    void setUp() {
        service = new CourseDuplicateDetectorService(webClient, courseRepository);
        ReflectionTestUtils.setField(service, "geminiApiKey", "dummy-key");
    }

    @Test
    void testGetEmbedding_MissingKey() {
        ReflectionTestUtils.setField(service, "geminiApiKey", "");
        assertThrows(IllegalStateException.class, () -> service.getEmbedding("test"));
    }

    @Test
    void testGetEmbedding_Success() {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        GeminiEmbeddingResponse response = new GeminiEmbeddingResponse();
        GeminiEmbeddingResponse.Embedding embedding = new GeminiEmbeddingResponse.Embedding();
        embedding.setValues(List.of(0.1, 0.2, 0.3));
        response.setEmbedding(embedding);

        when(responseSpec.bodyToMono(GeminiEmbeddingResponse.class)).thenReturn(Mono.just(response));

        List<Double> result = service.getEmbedding("test text");
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(0.1, result.get(0));
    }

    @Test
    void testSaveCourseEmbedding() {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        GeminiEmbeddingResponse response = new GeminiEmbeddingResponse();
        GeminiEmbeddingResponse.Embedding embedding = new GeminiEmbeddingResponse.Embedding();
        embedding.setValues(List.of(0.1, 0.2));
        response.setEmbedding(embedding);

        when(responseSpec.bodyToMono(GeminiEmbeddingResponse.class)).thenReturn(Mono.just(response));

        assertDoesNotThrow(() -> service.saveCourseEmbedding(1L, "text"));
        verify(courseRepository, times(1)).saveCourseEmbedding(eq(1L), eq("[0.1,0.2]"));
    }

    @Test
    void testCheckDuplication_Duplicate() {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        GeminiEmbeddingResponse response = new GeminiEmbeddingResponse();
        GeminiEmbeddingResponse.Embedding embedding = new GeminiEmbeddingResponse.Embedding();
        embedding.setValues(List.of(0.1, 0.2));
        response.setEmbedding(embedding);

        when(responseSpec.bodyToMono(GeminiEmbeddingResponse.class)).thenReturn(Mono.just(response));

        Object[] match = new Object[] { 2L, 0.95 };
        when(courseRepository.findDuplicateCourses(anyString(), eq(1L), eq(3))).thenReturn(java.util.Collections.singletonList(match));

        CourseDuplicateDetectorService.DuplicationCheckResult result = service.checkDuplication(1L, "text");

        assertTrue(result.isDuplicate());
        assertEquals(2L, result.getMatchedCourseId());
        assertEquals(0.95, result.getSimilarityScore());
    }

    @Test
    void testCheckDuplication_NotDuplicate() {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        GeminiEmbeddingResponse response = new GeminiEmbeddingResponse();
        GeminiEmbeddingResponse.Embedding embedding = new GeminiEmbeddingResponse.Embedding();
        embedding.setValues(List.of(0.1, 0.2));
        response.setEmbedding(embedding);

        when(responseSpec.bodyToMono(GeminiEmbeddingResponse.class)).thenReturn(Mono.just(response));

        Object[] match = new Object[] { 2L, 0.85 }; // Less than 0.90
        when(courseRepository.findDuplicateCourses(anyString(), eq(1L), eq(3))).thenReturn(java.util.Collections.singletonList(match));

        CourseDuplicateDetectorService.DuplicationCheckResult result = service.checkDuplication(1L, "text");

        assertFalse(result.isDuplicate());
        assertEquals(2L, result.getMatchedCourseId());
        assertEquals(0.85, result.getSimilarityScore());
    }
}
