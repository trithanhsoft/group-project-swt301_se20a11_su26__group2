package com.swp391.coding_platform.exception.ai;

import com.swp391.coding_platform.dto.response.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiVisualizerExceptionHandlerTest {

    private AiVisualizerExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new AiVisualizerExceptionHandler();
    }

    @Test
    void testHandleAiGenerationException() {
        AiGenerationException ex = new AiGenerationException("Timeout from Gemini API");
        
        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleAiGenerationException(ex);

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(5020, response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("Timeout from Gemini API"));
    }

    @Test
    void testHandleRateLimitException() {
        RateLimitExceededException ex = new RateLimitExceededException("Too many requests from this IP");
        
        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleRateLimitException(ex);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(4290, response.getBody().getCode());
        assertEquals("Too many requests from this IP", response.getBody().getMessage());
    }
}
