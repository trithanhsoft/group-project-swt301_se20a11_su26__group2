package com.swp391.coding_platform.exception;

import com.swp391.coding_platform.exception.ai.AiGenerationException;
import com.swp391.coding_platform.exception.ai.RateLimitExceededException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExceptionModelsTest {

    @Test
    void testAppException() {
        AppException ex = new AppException(ErrorCode.USER_NOT_FOUND);
        assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
        assertEquals(ErrorCode.USER_NOT_FOUND.getMessage(), ex.getMessage());
    }

    @Test
    void testAiGenerationException() {
        AiGenerationException ex = new AiGenerationException("Failed to generate AI");
        assertEquals("Failed to generate AI", ex.getMessage());
    }

    @Test
    void testRateLimitExceededException() {
        RateLimitExceededException ex = new RateLimitExceededException("Too many requests");
        assertEquals("Too many requests", ex.getMessage());
    }
}
