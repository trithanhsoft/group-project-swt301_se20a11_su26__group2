package com.swp391.coding_platform.exception;


import com.swp391.coding_platform.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Object>> appException(AppException exception){
        ErrorCode errorCode = exception.getErrorCode();
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResponse.builder()
                        .status(errorCode.getHttpStatus().value())
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .result(null)
                        .timestamp(Instant.now().toString())
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> inValidInput(MethodArgumentNotValidException exception){
        String enumKey = exception.getBindingResult().getFieldError().getDefaultMessage();
        ErrorCode errorCode = ErrorCode.valueOf(enumKey);
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ApiResponse.builder()
                        .status(errorCode.getHttpStatus().value())
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .result(null)
                        .timestamp(Instant.now().toString())
                        .build());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleTypeMismatch(MethodArgumentTypeMismatchException exception ){
        String paramName = exception.getName();
        String wrongValue = exception.getValue() != null ? exception.getValue().toString() : "null";
        String expectedType = exception.getRequiredType() != null ? exception.getRequiredType().getSimpleName() : "unknown";

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.builder()
                        .status(400)
                        .code(4000)
                        .message(String.format("Type mismatch for parameter '%s': value '%s' cannot be converted to '%s'.",
                                paramName, wrongValue, expectedType))
                        .result(null)
                        .timestamp(Instant.now().toString())
                        .build());
    }

    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handlingHttpMessageNotReadableException(HttpMessageNotReadableException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.<Object>builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .code(ErrorCode.INVALID_REQUEST_BODY.getCode())
                .message(ErrorCode.INVALID_REQUEST_BODY.getMessage())
                .result(null)
                .timestamp(Instant.now().toString())
                .build());
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ApiResponse<Object>> handlingException(Exception exception) {
        log.error("Lỗi hệ thống: ", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.<Object>builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .code(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode())
                .message("Lỗi hệ thống: " + exception.getMessage())
                .result(null)
                .timestamp(Instant.now().toString())
                .build());
    }

    @ExceptionHandler(value = Throwable.class)
    public ResponseEntity<ApiResponse<Object>> handlingThrowable(Throwable throwable) {
        log.error("Lỗi nghiêm trọng hệ thống: ", throwable);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.<Object>builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .code(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode())
                .message("Lỗi nghiêm trọng hệ thống: " + throwable.getMessage())
                .result(null)
                .timestamp(Instant.now().toString())
                .build());
    }
}
