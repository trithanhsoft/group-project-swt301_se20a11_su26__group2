package com.swp391.coding_platform.service.moderation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AudioProcessingServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    private AudioProcessingService audioProcessingService;

    @BeforeEach
    void setUp() {
        audioProcessingService = new AudioProcessingService("http://localhost:9000");
        ReflectionTestUtils.setField(audioProcessingService, "webClient", webClient);
    }

    @Test
    void testIsFfmpegAvailable_True() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(Map.of("ffmpeg", true)));

        assertTrue(audioProcessingService.isFfmpegAvailable());
    }

    @Test
    void testIsFfmpegAvailable_False() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.error(new RuntimeException("Connection error")));

        assertFalse(audioProcessingService.isFfmpegAvailable());
    }

    @Test
    void testExtractAudioFromVideo_Success() throws IOException, InterruptedException {
        String videoUrl = "http://video.com";
        File tempFile = File.createTempFile("test-audio", ".wav");
        tempFile.deleteOnExit();

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri("/extract-audio")).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(anyMap())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        
        byte[] mockBytes = "test-audio-content".getBytes();
        when(responseSpec.bodyToMono(byte[].class)).thenReturn(Mono.just(mockBytes));

        File result = audioProcessingService.extractAudioFromVideo(videoUrl, tempFile.getAbsolutePath());

        assertNotNull(result);
        assertTrue(result.exists());
        assertArrayEquals(mockBytes, Files.readAllBytes(result.toPath()));
    }
}
