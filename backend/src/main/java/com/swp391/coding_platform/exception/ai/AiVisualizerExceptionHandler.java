package com.swp391.coding_platform.exception.ai;

import com.swp391.coding_platform.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

@ControllerAdvice
public class AiVisualizerExceptionHandler {

    @ExceptionHandler(AiGenerationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAiGenerationException(AiGenerationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ApiResponse.builder()
                        .status(HttpStatus.BAD_GATEWAY.value())
                        .code(5020)
                        .message("AI không thể tạo mô phỏng cho bài này, vui lòng thử lại. Chi tiết: " + ex.getMessage())
                        .timestamp(Instant.now().toString())
                        .build());
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiResponse<Object>> handleRateLimitException(RateLimitExceededException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ApiResponse.builder()
                        .status(HttpStatus.TOO_MANY_REQUESTS.value())
                        .code(4290)
                        .message(ex.getMessage())
                        .timestamp(Instant.now().toString())
                        .build());
    }
}
