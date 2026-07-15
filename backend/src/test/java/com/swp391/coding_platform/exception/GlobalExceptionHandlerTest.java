package com.swp391.coding_platform.exception;

import com.swp391.coding_platform.dto.response.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void testAppException() {
        AppException ex = new AppException(ErrorCode.USER_NOT_FOUND);
        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.appException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.USER_NOT_FOUND.getCode(), response.getBody().getCode());
        assertEquals(ErrorCode.USER_NOT_FOUND.getMessage(), response.getBody().getMessage());
    }

    @Test
    void testInvalidInput() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("objectName", "field", "USERNAME_INVALID");
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldError()).thenReturn(fieldError);

        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.inValidInput(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.USERNAME_INVALID.getCode(), response.getBody().getCode());
        assertEquals(ErrorCode.USERNAME_INVALID.getMessage(), response.getBody().getMessage());
    }

    @Test
    void testHandleTypeMismatch() {
        MethodParameter parameter = mock(MethodParameter.class);
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "abc", Long.class, "id", parameter, new IllegalArgumentException("invalid format"));

        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleTypeMismatch(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(4000, response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("Type mismatch for parameter 'id'"));
    }

    @Test
    void testHandlingHttpMessageNotReadableException() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Invalid JSON format");

        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handlingHttpMessageNotReadableException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.INVALID_REQUEST_BODY.getCode(), response.getBody().getCode());
        assertEquals(ErrorCode.INVALID_REQUEST_BODY.getMessage(), response.getBody().getMessage());
    }

    @Test
    void testHandlingException() {
        Exception ex = new Exception("System crashed");

        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handlingException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode(), response.getBody().getCode());
        assertEquals("Lỗi hệ thống: System crashed", response.getBody().getMessage());
    }

    @Test
    void testHandlingThrowable() {
        Throwable ex = new OutOfMemoryError("Memory full");

        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handlingThrowable(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode(), response.getBody().getCode());
        assertEquals("Lỗi nghiêm trọng hệ thống: Memory full", response.getBody().getMessage());
    }
}
