package com.swp391.coding_platform.security;

import com.swp391.coding_platform.entity.problem.ProblemEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import com.swp391.coding_platform.entity.enums.EnrollmentStatus;
import com.swp391.coding_platform.entity.enums.ProblemScope;
import com.swp391.coding_platform.entity.instructor.InstructorEntity;
import com.swp391.coding_platform.repository.contest.ContestParticipantRepository;
import com.swp391.coding_platform.repository.course.CourseRepository;
import com.swp391.coding_platform.repository.course.EnrollmentRepository;
import com.swp391.coding_platform.repository.instructor.InstructorRepository;
import com.swp391.coding_platform.repository.problem.ProblemRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseSecurityTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private InstructorRepository instructorRepository;

    @Mock
    private ContestParticipantRepository contestParticipantRepository;

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private CourseSecurity courseSecurity;

    private static final long USER_ID = 1L;
    private static final long COURSE_ID = 10L;
    private static final long LESSON_ID = 20L;
    private static final long QUIZ_ID = 30L;
    private static final long PROBLEM_ID = 40L;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockSecurityContext(Long userId, String role) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getClaim("userId")).thenReturn(userId);

        if (role != null) {
            doReturn(List.of(new SimpleGrantedAuthority(role))).when(authentication).getAuthorities();
        }
    }

    private void mockUnauthenticated() {
        when(securityContext.getAuthentication()).thenReturn(null);
    }

    // --- canAccessCourse ---

    @Test
    void canAccessCourse_NullUserIdOrCourseId_ReturnsFalse() {
        mockUnauthenticated();
        assertFalse(courseSecurity.canAccessCourse(null));
    }

    @Test
    void canAccessCourse_IsAdmin_ReturnsTrue() {
        mockSecurityContext(USER_ID, "ROLE_ADMIN");
        assertTrue(courseSecurity.canAccessCourse(COURSE_ID));
    }

    @Test
    void canAccessCourse_IsEnrolled_ReturnsTrue() {
        mockSecurityContext(USER_ID, "ROLE_USER");
        when(enrollmentRepository.existsByUserIdAndCourseIdAndStatusIn(
                eq(USER_ID), eq(COURSE_ID), anyList())).thenReturn(true);

        assertTrue(courseSecurity.canAccessCourse(COURSE_ID));
    }

    @Test
    void canAccessCourse_IsInstructor_ReturnsTrue() {
        mockSecurityContext(USER_ID, "ROLE_INSTRUCTOR");
        when(enrollmentRepository.existsByUserIdAndCourseIdAndStatusIn(
                eq(USER_ID), eq(COURSE_ID), anyList())).thenReturn(false);

        InstructorEntity instructor = new InstructorEntity();
        instructor.setId(5);
        when(instructorRepository.findByUserId(1)).thenReturn(Optional.of(instructor));
        when(courseRepository.existsByIdAndInstructorId(COURSE_ID, 5)).thenReturn(true);

        assertTrue(courseSecurity.canAccessCourse(COURSE_ID));
    }

    @Test
    void canAccessCourse_NotEnrolledNotInstructor_ReturnsFalse() {
        mockSecurityContext(USER_ID, "ROLE_USER");
        when(enrollmentRepository.existsByUserIdAndCourseIdAndStatusIn(
                eq(USER_ID), eq(COURSE_ID), anyList())).thenReturn(false);
        when(instructorRepository.findByUserId(1)).thenReturn(Optional.empty());

        assertFalse(courseSecurity.canAccessCourse(COURSE_ID));
    }

    // --- canAccessLesson ---

    @Test
    void canAccessLesson_NullLessonId_ReturnsFalse() {
        mockUnauthenticated();
        assertFalse(courseSecurity.canAccessLesson((Long) null));
    }

    @Test
    void canAccessLesson_IsAdmin_ReturnsTrue() {
        mockSecurityContext(USER_ID, "ROLE_ADMIN");
        assertTrue(courseSecurity.canAccessLesson(LESSON_ID));
    }

    @Test
    void canAccessLesson_IsEnrolled_ReturnsTrue() {
        mockSecurityContext(USER_ID, "ROLE_USER");
        when(enrollmentRepository.isUserEnrolledInLesson(USER_ID, LESSON_ID)).thenReturn(true);

        assertTrue(courseSecurity.canAccessLesson(LESSON_ID));
    }

    @Test
    void canAccessLesson_IsInstructor_ReturnsTrue() {
        mockSecurityContext(USER_ID, "ROLE_INSTRUCTOR");
        when(enrollmentRepository.isUserEnrolledInLesson(USER_ID, LESSON_ID)).thenReturn(false);

        InstructorEntity instructor = new InstructorEntity();
        instructor.setId(5);
        when(instructorRepository.findByUserId(1)).thenReturn(Optional.of(instructor));
        when(courseRepository.existsByLessonIdAndInstructorId(LESSON_ID, 5)).thenReturn(true);

        assertTrue(courseSecurity.canAccessLesson(LESSON_ID));
    }

    // --- canAccessQuiz ---

    @Test
    void canAccessQuiz_NullQuizId_ReturnsFalse() {
        mockUnauthenticated();
        assertFalse(courseSecurity.canAccessQuiz((Long) null));
    }

    @Test
    void canAccessQuiz_IsAdmin_ReturnsTrue() {
        mockSecurityContext(USER_ID, "ROLE_ADMIN");
        assertTrue(courseSecurity.canAccessQuiz(QUIZ_ID));
    }

    @Test
    void canAccessQuiz_IsEnrolled_ReturnsTrue() {
        mockSecurityContext(USER_ID, "ROLE_USER");
        when(enrollmentRepository.isUserEnrolledInQuiz(USER_ID, QUIZ_ID)).thenReturn(true);

        assertTrue(courseSecurity.canAccessQuiz(QUIZ_ID));
    }

    // --- canAccessProblem ---

    @Test
    void canAccessProblem_NullProblemId_ReturnsFalse() {
        mockUnauthenticated();
        assertFalse(courseSecurity.canAccessProblem(null));
    }

    @Test
    void canAccessProblem_IsAdmin_ReturnsTrue() {
        mockSecurityContext(USER_ID, "ROLE_ADMIN");
        assertTrue(courseSecurity.canAccessProblem(PROBLEM_ID));
    }

    @Test
    void canAccessProblem_ProblemNotFound_ReturnsFalse() {
        mockSecurityContext(USER_ID, "ROLE_USER");
        when(problemRepository.findById(40)).thenReturn(Optional.empty());

        assertFalse(courseSecurity.canAccessProblem(PROBLEM_ID));
    }

    @Test
    void canAccessProblem_ProblemIsPublic_ReturnsTrue() {
        mockSecurityContext(USER_ID, "ROLE_USER");
        ProblemEntity problem = new ProblemEntity();
        problem.setIsPublic(true);
        when(problemRepository.findById(40)).thenReturn(Optional.of(problem));

        assertTrue(courseSecurity.canAccessProblem(PROBLEM_ID));
    }

    @Test
    void canAccessProblem_ProblemIsPractice_ReturnsTrue() {
        mockSecurityContext(USER_ID, "ROLE_USER");
        ProblemEntity problem = new ProblemEntity();
        problem.setProblemScope(ProblemScope.PRACTICE);
        when(problemRepository.findById(40)).thenReturn(Optional.of(problem));

        assertTrue(courseSecurity.canAccessProblem(PROBLEM_ID));
    }

    @Test
    void canAccessProblem_IsEnrolledInCourse_ReturnsTrue() {
        mockSecurityContext(USER_ID, "ROLE_USER");
        ProblemEntity problem = new ProblemEntity();
        problem.setIsPublic(false);
        problem.setProblemScope(ProblemScope.SHARED);
        when(problemRepository.findById(40)).thenReturn(Optional.of(problem));
        when(enrollmentRepository.isUserEnrolledByProblemId(USER_ID, PROBLEM_ID)).thenReturn(true);

        assertTrue(courseSecurity.canAccessProblem(PROBLEM_ID));
    }

    @Test
    void canAccessProblem_IsContestParticipant_ReturnsTrue() {
        mockSecurityContext(USER_ID, "ROLE_USER");
        ProblemEntity problem = new ProblemEntity();
        problem.setIsPublic(false);
        problem.setProblemScope(ProblemScope.CONTEST);
        when(problemRepository.findById(40)).thenReturn(Optional.of(problem));
        when(enrollmentRepository.isUserEnrolledByProblemId(USER_ID, PROBLEM_ID)).thenReturn(false);
        when(contestParticipantRepository.isUserParticipantOfProblemContest(1, 40)).thenReturn(true);

        assertTrue(courseSecurity.canAccessProblem(PROBLEM_ID));
    }

    @Test
    void canAccessProblem_IsCreator_ReturnsTrue() {
        mockSecurityContext(USER_ID, "ROLE_INSTRUCTOR");
        ProblemEntity problem = new ProblemEntity();
        problem.setIsPublic(false);
        problem.setProblemScope(ProblemScope.SHARED);
        UserEntity creator = new UserEntity();
        creator.setId(1);
        problem.setCreatedBy(creator);
        
        when(problemRepository.findById(40)).thenReturn(Optional.of(problem));
        when(enrollmentRepository.isUserEnrolledByProblemId(USER_ID, PROBLEM_ID)).thenReturn(false);
        when(contestParticipantRepository.isUserParticipantOfProblemContest(1, 40)).thenReturn(false);

        assertTrue(courseSecurity.canAccessProblem(PROBLEM_ID));
    }

    @Test
    void canAccessProblem_AccessDenied_ReturnsFalse() {
        mockSecurityContext(USER_ID, "ROLE_USER");
        ProblemEntity problem = new ProblemEntity();
        problem.setIsPublic(false);
        problem.setProblemScope(ProblemScope.SHARED);
        UserEntity creator = new UserEntity();
        creator.setId(2);
        problem.setCreatedBy(creator);
        
        when(problemRepository.findById(40)).thenReturn(Optional.of(problem));
        when(enrollmentRepository.isUserEnrolledByProblemId(USER_ID, PROBLEM_ID)).thenReturn(false);
        when(contestParticipantRepository.isUserParticipantOfProblemContest(1, 40)).thenReturn(false);

        assertFalse(courseSecurity.canAccessProblem(PROBLEM_ID));
    }
}
