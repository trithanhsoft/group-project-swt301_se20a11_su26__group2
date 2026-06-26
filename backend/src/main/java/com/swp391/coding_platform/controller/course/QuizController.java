package com.swp391.coding_platform.controller.course;

import com.swp391.coding_platform.dto.request.QuizSubmitRequest;
import com.swp391.coding_platform.dto.response.ApiResponse;
import com.swp391.coding_platform.dto.response.QuizDetailResponse;
import com.swp391.coding_platform.dto.response.QuizSubmitResultResponse;
import com.swp391.coding_platform.service.course.QuizService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QuizController {

    QuizService quizService;

    /**
     * 1. Lấy chi tiết đề thi trắc nghiệm TRỰC TIẾP bằng Lesson ID (KHÔNG CHỨA ĐÁP ÁN ĐÚNG nếu chưa nộp)
     * Thích hợp khi học sinh bấm vào phần Quiz của một bài học.
     * Tự động trả về kèm theo kết quả làm bài gần nhất của user nếu đã nộp.
     */
    @GetMapping("/{courseId}/lessons/{lessonId}/quiz")
    @PreAuthorize("@courseSecurity.canAccessLesson(#lessonId)")
    public ResponseEntity<ApiResponse<QuizDetailResponse>> getQuizByLesson(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("courseId") Long courseId,
            @PathVariable("lessonId") Integer lessonId) {

        if (jwt == null) {
            return ResponseEntity.status(401).build();
        }
        Number userIdClaim = jwt.getClaim("userId");
        if (userIdClaim == null) {
            return ResponseEntity.status(401).build();
        }
        Integer userId = userIdClaim.intValue();

        var result = quizService.getQuizDetailByLessonId(lessonId, userId);

        return ResponseEntity.ok(ApiResponse.<QuizDetailResponse>builder()
                .status(200)
                .code(1000)
                .message("Get quiz details successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    /**
     * 2. Nộp bài trắc nghiệm (Sử dụng quizId lấy ra từ QuizDetailResponse)
     */
    @PostMapping("/{courseId}/quizzes/{quizId}/submit")
    @PreAuthorize("@courseSecurity.canAccessQuiz(#quizId)")
    public ResponseEntity<ApiResponse<QuizSubmitResultResponse>> submitQuiz(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("courseId") Long courseId,
            @PathVariable("quizId") Integer quizId,
            @RequestBody @Valid QuizSubmitRequest request) {

        if (jwt == null) {
            return ResponseEntity.status(401).build();
        }
        Number userIdClaim = jwt.getClaim("userId");
        if (userIdClaim == null) {
            return ResponseEntity.status(401).build();
        }
        Integer userId = userIdClaim.intValue();

        var result = quizService.submitQuiz(quizId, userId, request);

        return ResponseEntity.ok(ApiResponse.<QuizSubmitResultResponse>builder()
                .status(200)
                .code(1000)
                .message("Submit quiz successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

}
