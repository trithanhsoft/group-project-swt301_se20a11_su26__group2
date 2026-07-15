package com.swp391.coding_platform.service.moderation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swp391.coding_platform.dto.moderation.GeminiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoTranscriptionServiceTest {

    @Mock
    private AudioProcessingService audioService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private VideoTranscriptionService transcriptionService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(transcriptionService, "openAiApiKey", "test-key");
        ReflectionTestUtils.setField(transcriptionService, "geminiApiKey", "test-gemini-key");
        ReflectionTestUtils.setField(transcriptionService, "geminiModel", "gemini-3.5-flash");
    }

    // ======================== Audio Extraction ========================

    @Test
    void transcribeVideoAsync_AudioExtractionFails_ReturnsError() throws Exception {
        Long courseId = 1L;
        Long lessonId = 2L;
        String videoUrl = "http://example.com/video.mp4";

        when(audioService.extractAudioFromVideo(eq(videoUrl), anyString())).thenThrow(new RuntimeException("Extraction Failed"));

        CompletableFuture<String> future = transcriptionService.transcribeVideoAsync(courseId, lessonId, videoUrl);
        String result = future.join();

        assertTrue(result.contains("[SYSTEM_ERROR: AUDIO_QUALITY_TOO_BAD]"));
        assertTrue(result.contains("Extraction Failed"));
    }

    @Test
    void transcribeVideoAsync_AudioTooSmall_ReturnsError() throws Exception {
        Long courseId = 1L;
        Long lessonId = 2L;
        String videoUrl = "http://example.com/video.mp4";

        File mockFile = File.createTempFile("test_audio", ".mp3");
        Files.write(mockFile.toPath(), "small".getBytes());

        when(audioService.extractAudioFromVideo(eq(videoUrl), anyString())).thenReturn(mockFile);

        CompletableFuture<String> future = transcriptionService.transcribeVideoAsync(courseId, lessonId, videoUrl);
        String result = future.join();

        assertEquals("[SYSTEM_ERROR: AUDIO_QUALITY_TOO_BAD] File âm thanh trích xuất bị lỗi hoặc rỗng.", result);
        mockFile.delete();
    }

    @Test
    void transcribeVideoAsync_WithoutGeminiKey_ReturnsMockTranscription() throws Exception {
        ReflectionTestUtils.setField(transcriptionService, "geminiApiKey", ""); // empty key

        Long courseId = 1L;
        Long lessonId = 2L;
        String videoUrl = "http://example.com/video.mp4";

        File mockFile = File.createTempFile("test_audio", ".mp3");
        byte[] dummyBytes = new byte[2048]; // Needs to be >= 1024 bytes
        Files.write(mockFile.toPath(), dummyBytes);

        when(audioService.extractAudioFromVideo(eq(videoUrl), anyString())).thenReturn(mockFile);

        CompletableFuture<String> future = transcriptionService.transcribeVideoAsync(courseId, lessonId, videoUrl);
        String result = future.join();

        assertEquals("Xin chào các bạn. Đây là video bài giảng về lập trình.", result);
        mockFile.delete();
    }

    // ======================== API Flow & Exceptions ========================

    @Test
    void transcribeVideoAsync_audioFileReadError_returnsFallbackMessage() throws Exception {
        Long courseId = 1L;
        Long lessonId = 2L;
        String videoUrl = "http://example.com/video.mp4";

        File mockFile = File.createTempFile("test_audio_read_error", ".mp3");
        byte[] dummyBytes = new byte[2048];
        Files.write(mockFile.toPath(), dummyBytes);

        when(audioService.extractAudioFromVideo(eq(videoUrl), anyString())).thenReturn(mockFile);

        // Mock Files.readAllBytes to throw IOException and CompletableFuture to run synchronously
        try (MockedStatic<CompletableFuture> completableFutureMock = mockStatic(CompletableFuture.class, Answers.CALLS_REAL_METHODS);
             MockedStatic<Files> filesMock = mockStatic(Files.class, Answers.CALLS_REAL_METHODS)) {
            
            completableFutureMock.when(() -> CompletableFuture.supplyAsync(any(java.util.function.Supplier.class)))
                    .thenAnswer(invocation -> {
                        java.util.function.Supplier<?> supplier = invocation.getArgument(0);
                        return CompletableFuture.completedFuture(supplier.get());
                    });

            filesMock.when(() -> Files.readAllBytes(any())).thenThrow(new IOException("Read error"));

            CompletableFuture<String> future = transcriptionService.transcribeVideoAsync(courseId, lessonId, videoUrl);
            String result = future.join();

            assertTrue(result.contains("Xin chào các bạn. Đây là video bài giảng về lập trình."));
        }

        mockFile.delete();
    }

    @Test
    void transcribeVideoAsync_withGeminiKey_returnsSuccessfulTranscription() throws Exception {
        Long courseId = 1L;
        Long lessonId = 2L;
        String videoUrl = "http://example.com/video.mp4";

        File mockFile = File.createTempFile("test_audio", ".mp3");
        byte[] dummyBytes = new byte[2048];
        Files.write(mockFile.toPath(), dummyBytes);

        when(audioService.extractAudioFromVideo(eq(videoUrl), anyString())).thenReturn(mockFile);

        // Prepare mock WebClient sequence
        WebClient webClient = mock(WebClient.class);
        WebClient.Builder builder = mock(WebClient.Builder.class);
        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(builder.codecs(any())).thenReturn(builder);
        when(builder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(java.net.URI.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        GeminiResponse mockResponse = new GeminiResponse();
        GeminiResponse.Candidate candidate = new GeminiResponse.Candidate();
        GeminiResponse.Content content = new GeminiResponse.Content();
        GeminiResponse.Part part = new GeminiResponse.Part();
        part.setText("This is the transcribed text from Gemini API.");
        content.setParts(List.of(part));
        candidate.setContent(content);
        mockResponse.setCandidates(List.of(candidate));

        when(responseSpec.bodyToMono(GeminiResponse.class)).thenReturn(Mono.just(mockResponse));

        try (MockedStatic<CompletableFuture> completableFutureMock = mockStatic(CompletableFuture.class, Answers.CALLS_REAL_METHODS);
             MockedStatic<WebClient> webClientMock = mockStatic(WebClient.class, Answers.CALLS_REAL_METHODS)) {
            
            completableFutureMock.when(() -> CompletableFuture.supplyAsync(any(java.util.function.Supplier.class)))
                    .thenAnswer(invocation -> {
                        java.util.function.Supplier<?> supplier = invocation.getArgument(0);
                        return CompletableFuture.completedFuture(supplier.get());
                    });

            webClientMock.when(WebClient::builder).thenReturn(builder);

            CompletableFuture<String> future = transcriptionService.transcribeVideoAsync(courseId, lessonId, videoUrl);
            String result = future.join();

            assertEquals("This is the transcribed text from Gemini API.", result);
        }

        mockFile.delete();
    }

    @Test
    void transcribeVideoAsync_geminiSpeechNotDetected_returnsSystemError() throws Exception {
        Long courseId = 1L;
        Long lessonId = 2L;
        String videoUrl = "http://example.com/video.mp4";

        File mockFile = File.createTempFile("test_audio", ".mp3");
        byte[] dummyBytes = new byte[2048];
        Files.write(mockFile.toPath(), dummyBytes);

        when(audioService.extractAudioFromVideo(eq(videoUrl), anyString())).thenReturn(mockFile);

        WebClient webClient = mock(WebClient.class);
        WebClient.Builder builder = mock(WebClient.Builder.class);
        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(builder.codecs(any())).thenReturn(builder);
        when(builder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(java.net.URI.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        GeminiResponse mockResponse = new GeminiResponse();
        GeminiResponse.Candidate candidate = new GeminiResponse.Candidate();
        GeminiResponse.Content content = new GeminiResponse.Content();
        GeminiResponse.Part part = new GeminiResponse.Part();
        part.setText("[NO_SPEECH_DETECTED] within this audio segment.");
        content.setParts(List.of(part));
        candidate.setContent(content);
        mockResponse.setCandidates(List.of(candidate));

        when(responseSpec.bodyToMono(GeminiResponse.class)).thenReturn(Mono.just(mockResponse));

        try (MockedStatic<CompletableFuture> completableFutureMock = mockStatic(CompletableFuture.class, Answers.CALLS_REAL_METHODS);
             MockedStatic<WebClient> webClientMock = mockStatic(WebClient.class, Answers.CALLS_REAL_METHODS)) {
            
            completableFutureMock.when(() -> CompletableFuture.supplyAsync(any(java.util.function.Supplier.class)))
                    .thenAnswer(invocation -> {
                        java.util.function.Supplier<?> supplier = invocation.getArgument(0);
                        return CompletableFuture.completedFuture(supplier.get());
                    });

            webClientMock.when(WebClient::builder).thenReturn(builder);

            CompletableFuture<String> future = transcriptionService.transcribeVideoAsync(courseId, lessonId, videoUrl);
            String result = future.join();

            assertTrue(result.contains("[SYSTEM_ERROR: AUDIO_QUALITY_TOO_BAD]"));
            assertTrue(result.contains("Không tìm thấy tiếng người nói"));
        }

        mockFile.delete();
    }

    @Test
    void transcribeVideoAsync_geminiHttp503ThenSuccess_returnsTranscription() throws Exception {
        Long courseId = 1L;
        Long lessonId = 2L;
        String videoUrl = "http://example.com/video.mp4";

        File mockFile = File.createTempFile("test_audio", ".mp3");
        byte[] dummyBytes = new byte[2048];
        Files.write(mockFile.toPath(), dummyBytes);

        when(audioService.extractAudioFromVideo(eq(videoUrl), anyString())).thenReturn(mockFile);

        WebClient webClient = mock(WebClient.class);
        WebClient.Builder builder = mock(WebClient.Builder.class);
        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(builder.codecs(any())).thenReturn(builder);
        when(builder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(java.net.URI.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        GeminiResponse mockResponse = new GeminiResponse();
        GeminiResponse.Candidate candidate = new GeminiResponse.Candidate();
        GeminiResponse.Content content = new GeminiResponse.Content();
        GeminiResponse.Part part = new GeminiResponse.Part();
        part.setText("Hello, world after retry.");
        content.setParts(List.of(part));
        candidate.setContent(content);
        mockResponse.setCandidates(List.of(candidate));

        // First attempt: HTTP 503 Service Unavailable Exception
        WebClientResponseException ex503 = mock(WebClientResponseException.class);
        when(ex503.getMessage()).thenReturn("503 Service Unavailable");
        when(ex503.getResponseBodyAsString()).thenReturn("Busy server");

        // Second attempt: succeeds
        when(responseSpec.bodyToMono(GeminiResponse.class))
                .thenReturn(Mono.error(ex503))
                .thenReturn(Mono.just(mockResponse));

        try (MockedStatic<CompletableFuture> completableFutureMock = mockStatic(CompletableFuture.class, Answers.CALLS_REAL_METHODS);
             MockedStatic<WebClient> webClientMock = mockStatic(WebClient.class, Answers.CALLS_REAL_METHODS)) {
            
            completableFutureMock.when(() -> CompletableFuture.supplyAsync(any(java.util.function.Supplier.class)))
                    .thenAnswer(invocation -> {
                        java.util.function.Supplier<?> supplier = invocation.getArgument(0);
                        return CompletableFuture.completedFuture(supplier.get());
                    });

            webClientMock.when(WebClient::builder).thenReturn(builder);

            CompletableFuture<String> future = transcriptionService.transcribeVideoAsync(courseId, lessonId, videoUrl);
            String result = future.join();

            assertEquals("Hello, world after retry.", result);
        }

        mockFile.delete();
    }
}
