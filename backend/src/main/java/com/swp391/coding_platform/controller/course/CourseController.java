package com.swp391.coding_platform.controller.course;

import com.swp391.coding_platform.dto.request.CourseSearchRequest;
import com.swp391.coding_platform.dto.response.ApiResponse;
import com.swp391.coding_platform.dto.response.CourseListItemResponse;
import com.swp391.coding_platform.dto.response.CourseDetailResponse;
import com.swp391.coding_platform.dto.response.CurriculumChapterResponse;
import com.swp391.coding_platform.dto.response.CourseReviewStatsResponse;
import com.swp391.coding_platform.dto.request.CourseReviewRequest;
import com.swp391.coding_platform.dto.response.PageResponse;
import com.swp391.coding_platform.dto.response.LearningDetailResponse;
import com.swp391.coding_platform.dto.response.LearningLessonResponse;
import com.swp391.coding_platform.dto.response.LearningCurriculumChapterResponse;
import com.swp391.coding_platform.dto.request.CreateCommentRequest;
import com.swp391.coding_platform.dto.response.LessonCommentResponse;
import com.swp391.coding_platform.service.course.CourseService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.PageRequest;
import java.util.List;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@Slf4j
@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CourseController {

    CourseService courseService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CourseListItemResponse>>> getCourseList(
            @AuthenticationPrincipal Jwt jwt,
            @Valid CourseSearchRequest courseSearchRequest){

        Integer userId = null;
        if(jwt != null){
            Number idClaim = jwt.getClaim("userId");
            if (idClaim != null) userId = idClaim.intValue();
        }

        Pageable pageable = courseSearchRequest.getPageable();

        var result = courseService.
                getCourseList(userId != null ? userId.longValue() : null, courseSearchRequest, pageable);

        return ResponseEntity.ok(ApiResponse.<PageResponse<CourseListItemResponse>>builder()
                .status(200)
                .code(1000)
                .message("Get course list successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseDetailResponse>> getCourseDetail(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") Long id) {

        Long userId = null;
        if (jwt != null) {
            Number idClaim = jwt.getClaim("userId");
            if (idClaim != null) {
                userId = idClaim.longValue();
            }
        }

        var result = courseService.getCourseDetail(userId, id);

        return ResponseEntity.ok(ApiResponse.<CourseDetailResponse>builder()
                .status(200)
                .code(1000)
                .message("Get course detail successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @GetMapping("/{id}/curriculum")
    public ResponseEntity<ApiResponse<List<CurriculumChapterResponse>>> getCourseCurriculum(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") Long id) {

        Long userId = null;
        if (jwt != null) {
            Number idClaim = jwt.getClaim("userId");
            if (idClaim != null) {
                userId = idClaim.longValue();
            }
        }

        var result = courseService.getCourseCurriculum(userId, id);

        return ResponseEntity.ok(ApiResponse.<List<CurriculumChapterResponse>>builder()
                .status(200)
                .code(1000)
                .message("Get course curriculum successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<ApiResponse<CourseReviewStatsResponse>> getCourseReviews(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") Long id,
            org.springframework.data.domain.Pageable pageable) {

        Long userId = null;
        if (jwt != null) {
            Number idClaim = jwt.getClaim("userId");
            if (idClaim != null) userId = idClaim.longValue();
        }

        var result = courseService.getCourseReviews(id, userId, pageable);

        return ResponseEntity.ok(ApiResponse.<CourseReviewStatsResponse>builder()
                .status(200)
                .code(1000)
                .message("Get course reviews successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @PostMapping("/{id}/reviews")
    public ResponseEntity<ApiResponse<Void>> upsertCourseReview(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") Long id,
            @RequestBody @Valid CourseReviewRequest request) {

        if (jwt == null) {
            return ResponseEntity.status(401).build();
        }

        Number idClaim = jwt.getClaim("userId");
        if (idClaim == null) {
            return ResponseEntity.status(401).build();
        }

        courseService.upsertCourseReview(id, idClaim.longValue(), request);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(200)
                .code(1000)
                .message("Review submitted successfully")
                .timestamp(Instant.now().toString())
                .build());
    }

    @GetMapping("/{id}/learning-detail")
    @PreAuthorize("@courseSecurity.canAccessCourse(#id)")
    public ResponseEntity<ApiResponse<LearningDetailResponse>> getCourseLearningDetail(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") Long id) {

        if (jwt == null) {
            return ResponseEntity.status(401).build();
        }
        Number idClaim = jwt.getClaim("userId");
        if (idClaim == null) {
            return ResponseEntity.status(401).build();
        }

        var result = courseService.getCourseLearningDetail(idClaim.longValue(), id);

        return ResponseEntity.ok(ApiResponse.<LearningDetailResponse>builder()
                .status(200)
                .code(1000)
                .message("Get learning details successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @GetMapping("/{id}/learning-curriculum")
    @PreAuthorize("@courseSecurity.canAccessCourse(#id)")
    public ResponseEntity<ApiResponse<List<LearningCurriculumChapterResponse>>> getCourseLearningCurriculum(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") Long id) {

        if (jwt == null) {
            return ResponseEntity.status(401).build();
        }
        Number idClaim = jwt.getClaim("userId");
        if (idClaim == null) {
            return ResponseEntity.status(401).build();
        }

        var result = courseService.getCourseLearningCurriculum(idClaim.longValue(), id);

        return ResponseEntity.ok(ApiResponse.<List<LearningCurriculumChapterResponse>>builder()
                .status(200)
                .code(1000)
                .message("Get learning curriculum successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @GetMapping("/{id}/lessons/{lessonId}")
    @PreAuthorize("@courseSecurity.canAccessLesson(#lessonId)")
    public ResponseEntity<ApiResponse<LearningLessonResponse>> getLearningLessonDetail(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") Long courseId,
            @PathVariable("lessonId") Integer lessonId) {

        if (jwt == null) {
            return ResponseEntity.status(401).build();
        }
        Number idClaim = jwt.getClaim("userId");
        if (idClaim == null) {
            return ResponseEntity.status(401).build();
        }

        var result = courseService.getLearningLessonDetail(idClaim.longValue(), courseId, lessonId);

        return ResponseEntity.ok(ApiResponse.<LearningLessonResponse>builder()
                .status(200)
                .code(1000)
                .message("Get lesson details successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @GetMapping("/lessons/{lessonId}/comments")
    @PreAuthorize("@courseSecurity.canAccessLesson(#lessonId)")
    public ResponseEntity<ApiResponse<List<LessonCommentResponse>>> getLessonComments(
            @PathVariable("lessonId") Integer lessonId) {

        var result = courseService.getLessonComments(lessonId);

        return ResponseEntity.ok(ApiResponse.<List<LessonCommentResponse>>builder()
                .status(200)
                .code(1000)
                .message("Get lesson comments successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @PostMapping("/lessons/{lessonId}/comments")
    @PreAuthorize("@courseSecurity.canAccessLesson(#lessonId)")
    public ResponseEntity<ApiResponse<LessonCommentResponse>> addLessonComment(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("lessonId") Integer lessonId,
            @Valid @RequestBody CreateCommentRequest request) {

        if (jwt == null) {
            return ResponseEntity.status(401).build();
        }
        Number idClaim = jwt.getClaim("userId");
        if (idClaim == null) {
            return ResponseEntity.status(401).build();
        }

        var result = courseService.addLessonComment(lessonId, idClaim.intValue(), request);

        return ResponseEntity.ok(ApiResponse.<LessonCommentResponse>builder()
                .status(200)
                .code(1000)
                .message("Comment posted successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @PostMapping("/{id}/lessons/{lessonId}/complete")
    @PreAuthorize("@courseSecurity.canAccessCourse(#id) && @courseSecurity.canAccessLesson(#lessonId)")
    public ResponseEntity<ApiResponse<Void>> completeLesson(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") Long id,
            @PathVariable("lessonId") Integer lessonId) {

        if (jwt == null) {
            return ResponseEntity.status(401).build();
        }
        Number idClaim = jwt.getClaim("userId");
        if (idClaim == null) {
            return ResponseEntity.status(401).build();
        }

        courseService.completeLesson(idClaim.longValue(), id, lessonId);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(200)
                .code(1000)
                .message("Lesson completed successfully")
                .timestamp(Instant.now().toString())
                .build());
    }
}

