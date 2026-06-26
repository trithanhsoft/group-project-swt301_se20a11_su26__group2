package com.swp391.coding_platform.security;

import com.swp391.coding_platform.repository.course.EnrollmentRepository;
import com.swp391.coding_platform.repository.course.CourseRepository;
import com.swp391.coding_platform.repository.instructor.InstructorRepository;
import com.swp391.coding_platform.repository.contest.ContestParticipantRepository;
import com.swp391.coding_platform.repository.problem.ProblemRepository;
import com.swp391.coding_platform.entity.enums.EnrollmentStatus;
import com.swp391.coding_platform.entity.enums.ProblemScope;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component("courseSecurity")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CourseSecurity {
    EnrollmentRepository enrollmentRepository;
    CourseRepository courseRepository;
    InstructorRepository instructorRepository;
    ContestParticipantRepository contestParticipantRepository;
    ProblemRepository problemRepository;

    public boolean canAccessCourse(Long courseId) {
        Long userId = getCurrentUserId();
        log.info("[CourseSecurity] canAccessCourse - userId: {}, courseId: {}", userId, courseId);
        if (userId == null || courseId == null) {
            log.warn("[CourseSecurity] canAccessCourse - userId or courseId is null!");
            return false;
        }

        // 1. Quyền ADMIN được phép truy cập
        if (isCurrentUserAdmin()) {
            log.info("[CourseSecurity] canAccessCourse - user is admin, granting access");
            return true;
        }

        // 2. Kiểm tra xem học viên có đang đăng ký khóa học này (ACTIVE hoặc COMPLETED)
        boolean isEnrolled = enrollmentRepository.existsByUserIdAndCourseIdAndStatusIn(
                userId, courseId, List.of(EnrollmentStatus.ACTIVE, EnrollmentStatus.COMPLETED)
        );
        if (isEnrolled) {
            log.info("[CourseSecurity] canAccessCourse - user is enrolled");
            return true;
        }

        // 3. Kiểm tra xem user có phải là giáo viên của khóa học này không
        var instructorOpt = instructorRepository.findByUserId(userId.intValue());
        if (instructorOpt.isPresent()) {
            boolean isInstructor = courseRepository.existsByIdAndInstructorId(courseId, instructorOpt.get().getId());
            if (isInstructor) {
                log.info("[CourseSecurity] canAccessCourse - user is the course instructor");
                return true;
            }
        }

        log.warn("[CourseSecurity] canAccessCourse - access denied");
        return false;
    }

    public boolean canAccessLesson(Long lessonId) {
        Long userId = getCurrentUserId();
        log.info("[CourseSecurity] canAccessLesson - userId: {}, lessonId: {}", userId, lessonId);
        if (userId == null || lessonId == null) {
            log.warn("[CourseSecurity] canAccessLesson - userId or lessonId is null!");
            return false;
        }

        if (isCurrentUserAdmin()) {
            log.info("[CourseSecurity] canAccessLesson - user is admin, granting access");
            return true;
        }

        // 1. Học viên có tham gia khóa học chứa bài học này hay không
        boolean isEnrolled = enrollmentRepository.isUserEnrolledInLesson(userId, lessonId);
        if (isEnrolled) {
            log.info("[CourseSecurity] canAccessLesson - student is enrolled");
            return true;
        }

        // 2. Giáo viên sở hữu khóa học chứa bài học này
        var instructorOpt = instructorRepository.findByUserId(userId.intValue());
        if (instructorOpt.isPresent()) {
            boolean isInstructor = courseRepository.existsByLessonIdAndInstructorId(lessonId, instructorOpt.get().getId());
            if (isInstructor) {
                log.info("[CourseSecurity] canAccessLesson - user is the instructor");
                return true;
            }
        }

        log.warn("[CourseSecurity] canAccessLesson - access denied");
        return false;
    }

    public boolean canAccessLesson(Integer lessonId) {
        return canAccessLesson(lessonId == null ? null : lessonId.longValue());
    }

    public boolean canAccessQuiz(Long quizId) {
        Long userId = getCurrentUserId();
        log.info("[CourseSecurity] canAccessQuiz - userId: {}, quizId: {}", userId, quizId);
        if (userId == null || quizId == null) {
            log.warn("[CourseSecurity] canAccessQuiz - userId or quizId is null!");
            return false;
        }

        if (isCurrentUserAdmin()) {
            return true;
        }

        // 1. Học viên có tham gia khóa học chứa bài học này hay không
        boolean isEnrolled = enrollmentRepository.isUserEnrolledInQuiz(userId, quizId);
        if (isEnrolled) {
            return true;
        }

        // 2. Giáo viên sở hữu khóa học chứa bài học này
        var instructorOpt = instructorRepository.findByUserId(userId.intValue());
        if (instructorOpt.isPresent()) {
            boolean isInstructor = courseRepository.existsByQuizIdAndInstructorId(quizId, instructorOpt.get().getId());
            if (isInstructor) {
                return true;
            }
        }

        return false;
    }

    public boolean canAccessQuiz(Integer quizId) {
        return canAccessQuiz(quizId == null ? null : quizId.longValue());
    }

    public boolean canAccessProblem(Long problemId) {
        Long userId = getCurrentUserId();
        log.info("[CourseSecurity] canAccessProblem - userId: {}, problemId: {}", userId, problemId);
        if (userId == null || problemId == null) {
            return false;
        }

        if (isCurrentUserAdmin()) {
            return true;
        }

        var problemOpt = problemRepository.findById(problemId.intValue());
        if (problemOpt.isEmpty()) {
            return false;
        }

        var problem = problemOpt.get();

        // 1. Bài tập public hoặc bài tập luyện tập tự do
        if (Boolean.TRUE.equals(problem.getIsPublic()) || ProblemScope.PRACTICE.equals(problem.getProblemScope())) {
            return true;
        }

        // 2. Học viên mua khóa học có chứa bài học chứa bài tập này
        if (enrollmentRepository.isUserEnrolledByProblemId(userId, problemId)) {
            return true;
        }

        // 3. Học viên tham gia kì thi có bài tập này
        boolean isContestParticipant = contestParticipantRepository.isUserParticipantOfProblemContest(userId.intValue(), problemId.intValue());
        if (isContestParticipant) {
            return true;
        }

        // 4. Giáo viên tạo bài tập này
        if (problem.getCreatedBy() != null && userId.intValue() == problem.getCreatedBy().getId()) {
            return true;
        }

        return false;
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof Jwt jwt)) {
            return null;
        }
        Object userIdClaim = jwt.getClaim("userId");
        if (userIdClaim instanceof Number) {
            return ((Number) userIdClaim).longValue();
        }
        return null;
    }

    private boolean isCurrentUserAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ADMIN".equals(a.getAuthority()) || "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
