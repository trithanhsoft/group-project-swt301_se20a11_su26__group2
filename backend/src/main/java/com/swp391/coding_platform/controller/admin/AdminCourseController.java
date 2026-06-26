package com.swp391.coding_platform.controller.admin;

import com.swp391.coding_platform.dto.response.AdminCourseResponse;
import com.swp391.coding_platform.dto.response.ApiResponse;
import com.swp391.coding_platform.entity.course.CourseEntity;
import com.swp391.coding_platform.entity.enums.CourseStatus;
import com.swp391.coding_platform.repository.course.CourseRepository;
import com.swp391.coding_platform.repository.course.ChapterRepository;
import lombok.RequiredArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/admin/courses")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminCourseController {

    private final CourseRepository courseRepository;
    private final ChapterRepository chapterRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminCourseResponse>>> getCourses() {
        log.info("Admin retrieving all courses for review");

        List<CourseEntity> courses = courseRepository.findAll();
        List<AdminCourseResponse> result = courses.stream().map(c -> {
            int totalChapters = chapterRepository.countByCourseId(c.getId());

            Integer instructorId = null;
            String instructorName = "Unknown";
            String instructorAvatarUrl = null;

            if (c.getInstructor() != null) {
                instructorId = c.getInstructor().getId();
                instructorName = c.getInstructor().getFullName();
                if (c.getInstructor().getUser() != null) {
                    instructorAvatarUrl = c.getInstructor().getUser().getAvatarurl();
                }
            }

            return AdminCourseResponse.builder()
                    .id(c.getId())
                    .instructorId(instructorId)
                    .instructorName(instructorName)
                    .instructorAvatarUrl(instructorAvatarUrl)
                    .title(c.getTitle())
                    .thumbnailUrl(c.getThumbnailUrl())
                    .shortDescription(c.getShortDescription())
                    .longDescription(c.getLongDescription())
                    .status(c.getStatus().name())
                    .price(c.getPrice())
                    .averageRating(c.getAverageRating())
                    .totalReviews(c.getTotalReviews())
                    .totalEnrolled(c.getTotalEnrolled())
                    .totalLessons(c.getTotalLessons())
                    .totalQuizzes(c.getTotalQuizzes())
                    .totalVideos(c.getTotalVideos())
                    .totalChapters(totalChapters)
                    .build();
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.<List<AdminCourseResponse>>builder()
                .status(200)
                .code(1000)
                .message("Fetched admin courses successfully")
                .result(result)
                .timestamp(Instant.now().toString())
                .build());
    }

    @Getter
    @Setter
    static class ApproveRequest {
        private String status; // "APPROVED" or "REJECTED"
        private String adminNote;
    }

    /**
     * Admin approves or rejects a PENDING course.
     * POST /admin/courses/{courseId}/approve
     * Body: { "status": "APPROVED" | "REJECTED", "adminNote": "..." }
     */
    @PostMapping("/{courseId}/approve")
    public ResponseEntity<ApiResponse<AdminCourseResponse>> approveCourse(
            @PathVariable Long courseId,
            @RequestBody ApproveRequest request) {

        log.info("Admin updating course {} status to {}", courseId, request.getStatus());

        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found: " + courseId));

        CourseStatus newStatus;
        try {
            newStatus = CourseStatus.valueOf(request.getStatus());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.<AdminCourseResponse>builder()
                    .status(400)
                    .code(4000)
                    .message("Invalid status: " + request.getStatus() + ". Must be APPROVED or REJECTED.")
                    .timestamp(Instant.now().toString())
                    .build());
        }

        if (newStatus != CourseStatus.APPROVED && newStatus != CourseStatus.REJECTED) {
            return ResponseEntity.badRequest().body(ApiResponse.<AdminCourseResponse>builder()
                    .status(400)
                    .code(4000)
                    .message("Only APPROVED or REJECTED status allowed via this endpoint.")
                    .timestamp(Instant.now().toString())
                    .build());
        }

        course.setStatus(newStatus);
        course.setUpdatedAt(Instant.now());
        courseRepository.save(course);

        log.info("Admin successfully updated course {} to status {}", courseId, newStatus);

        int totalChapters = chapterRepository.countByCourseId(course.getId());
        Integer instructorId = null;
        String instructorName = "Unknown";
        String instructorAvatarUrl = null;

        if (course.getInstructor() != null) {
            instructorId = course.getInstructor().getId();
            instructorName = course.getInstructor().getFullName();
            if (course.getInstructor().getUser() != null) {
                instructorAvatarUrl = course.getInstructor().getUser().getAvatarurl();
            }
        }

        AdminCourseResponse response = AdminCourseResponse.builder()
                .id(course.getId())
                .instructorId(instructorId)
                .instructorName(instructorName)
                .instructorAvatarUrl(instructorAvatarUrl)
                .title(course.getTitle())
                .thumbnailUrl(course.getThumbnailUrl())
                .shortDescription(course.getShortDescription())
                .longDescription(course.getLongDescription())
                .status(course.getStatus().name())
                .price(course.getPrice())
                .averageRating(course.getAverageRating())
                .totalReviews(course.getTotalReviews())
                .totalEnrolled(course.getTotalEnrolled())
                .totalLessons(course.getTotalLessons())
                .totalQuizzes(course.getTotalQuizzes())
                .totalVideos(course.getTotalVideos())
                .totalChapters(totalChapters)
                .build();

        return ResponseEntity.ok(ApiResponse.<AdminCourseResponse>builder()
                .status(200)
                .code(1000)
                .message("Course " + newStatus.name().toLowerCase() + " successfully")
                .result(response)
                .timestamp(Instant.now().toString())
                .build());
    }
}
