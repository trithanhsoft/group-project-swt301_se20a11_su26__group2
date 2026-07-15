package com.swp391.coding_platform.service.user;

import com.swp391.coding_platform.dto.response.CourseListItemResponse;
import com.swp391.coding_platform.dto.response.DashboardStatsResponse;
import com.swp391.coding_platform.dto.response.SubmissionStatisticResponse;
import com.swp391.coding_platform.dto.response.UserActivityResponse;
import com.swp391.coding_platform.entity.course.CourseEntity;
import com.swp391.coding_platform.entity.enums.EnrollmentStatus;
import com.swp391.coding_platform.entity.enums.OjVerdict;
import com.swp391.coding_platform.entity.payment.WalletEntity;
import com.swp391.coding_platform.entity.progress.CompletedLessonsCountEntity;
import com.swp391.coding_platform.exception.AppException;
import com.swp391.coding_platform.exception.ErrorCode;
import com.swp391.coding_platform.mapper.CourseMapper;
import com.swp391.coding_platform.repository.course.EnrollmentRepository;
import com.swp391.coding_platform.repository.course.LessonRepository;
import com.swp391.coding_platform.repository.payment.WalletRepository;
import com.swp391.coding_platform.repository.progress.CompletedLessonCountRepository;
import com.swp391.coding_platform.repository.user.UserDailyActivityRepository;
import com.swp391.coding_platform.repository.user.UserRepository;
import com.swp391.coding_platform.repository.problem.ProblemSubmissionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompletedLessonCountRepository completedLessonCountRepository;

    @Mock
    private UserDailyActivityRepository activityRepository;

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private ProblemSubmissionRepository problemSubmissionRepository;

    @Mock
    private LessonRepository lessonRepository;

    @InjectMocks
    private DashboardService dashboardService;

    // ======================== getDashboardStats ========================

    @Test
    void getDashboardStats_HappyPath() {
        Integer userId = 1;
        when(enrollmentRepository.countByUserId(userId)).thenReturn(5L);
        when(enrollmentRepository.countByUserIdAndStatus(anyInt(), any())).thenReturn(2L);
        when(userRepository.countSolvedPracticeProblemsByUserId(userId)).thenReturn(10L);
        when(userRepository.countTotalPracticeProblems()).thenReturn(50L);
        
        WalletEntity wallet = new WalletEntity();
        wallet.setBalance(new BigDecimal("150.00"));
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
        
        when(userRepository.getUserRanking(userId)).thenReturn(5);
        when(userRepository.count()).thenReturn(100L);

        DashboardStatsResponse response = dashboardService.getDashboardStats(userId);

        assertNotNull(response);
        assertEquals(5L, response.getEnrolled());
        assertEquals(2L, response.getCompletedCourses());
        assertEquals(10L, response.getSolvedPractice());
        assertEquals(50L, response.getTotalPracticeProblems());
        assertEquals(new BigDecimal("150.00"), response.getCurrentBalance());
        assertEquals(5L, response.getRanking());
        assertEquals(100L, response.getTotalUsers());
    }

    @Test
    void getDashboardStats_nullStatsAndEmptyWallet_returnsDefaults() {
        Integer userId = 1;
        when(enrollmentRepository.countByUserId(userId)).thenReturn(0L);
        when(enrollmentRepository.countByUserIdAndStatus(anyInt(), any())).thenReturn(0L);
        when(userRepository.countSolvedPracticeProblemsByUserId(userId)).thenReturn(null);
        when(userRepository.countTotalPracticeProblems()).thenReturn(null);
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userRepository.getUserRanking(userId)).thenReturn(null);
        when(userRepository.count()).thenReturn(0L); // primitive return type must be non-null long

        DashboardStatsResponse response = dashboardService.getDashboardStats(userId);

        assertNotNull(response);
        assertEquals(0L, response.getSolvedPractice());
        assertEquals(0L, response.getTotalPracticeProblems());
        assertEquals(BigDecimal.ZERO, response.getCurrentBalance());
        assertEquals(0L, response.getRanking());
        assertEquals(0L, response.getTotalUsers());
    }

    @Test
    void getDashboardStats_ExceptionThrown_throwsAppException() {
        Integer userId = 1;
        when(enrollmentRepository.countByUserId(userId)).thenThrow(new RuntimeException("DB down"));

        assertThrows(RuntimeException.class, () -> dashboardService.getDashboardStats(userId));
    }

    // ======================== getEnrolledCourses ========================

    @Test
    void getEnrolledCourses_emptyActiveCourses_returnsEmptyList() {
        when(enrollmentRepository.findActiveCoursesByUserId(1L)).thenReturn(Collections.emptySet());

        List<CourseListItemResponse> result = dashboardService.getEnrolledCourses(1);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getEnrolledCourses_validActiveCourses_calculatesProgress() {
        CourseEntity course = new CourseEntity();
        course.setId(10L);
        course.setTitle("Java Programming");

        when(enrollmentRepository.findActiveCoursesByUserId(1L)).thenReturn(new HashSet<>(List.of(course)));

        CompletedLessonsCountEntity countEntity = new CompletedLessonsCountEntity();
        countEntity.setCourse(course);
        countEntity.setCompletedLessonsCount(4);
        when(completedLessonCountRepository.findByUserIdAndCourseIdIn(eq(1), anySet()))
                .thenReturn(List.of(countEntity));

        List<Object[]> lessonCountsRaw = new ArrayList<>();
        lessonCountsRaw.add(new Object[]{10L, 8});
        when(lessonRepository.countLessonsByCourseIds(anySet()))
                .thenReturn(lessonCountsRaw);

        CourseListItemResponse itemResponse = new CourseListItemResponse();
        itemResponse.setId(10L);
        when(courseMapper.toCourseListItemResponse(course)).thenReturn(itemResponse);

        List<CourseListItemResponse> result = dashboardService.getEnrolledCourses(1);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getEnrolled());
        assertEquals(50, result.get(0).getProgressPercentage()); // 4 completed out of 8 = 50%
    }

    @Test
    void getEnrolledCourses_completedLessonsNull_defaultsToZero() {
        CourseEntity course = new CourseEntity();
        course.setId(10L);

        when(enrollmentRepository.findActiveCoursesByUserId(1L)).thenReturn(new HashSet<>(List.of(course)));

        CompletedLessonsCountEntity countEntity = new CompletedLessonsCountEntity();
        countEntity.setCourse(course);
        countEntity.setCompletedLessonsCount(null); // Null completed count
        when(completedLessonCountRepository.findByUserIdAndCourseIdIn(eq(1), anySet()))
                .thenReturn(List.of(countEntity));

        List<Object[]> lessonCountsRaw = new ArrayList<>();
        lessonCountsRaw.add(new Object[]{10L, 5});
        when(lessonRepository.countLessonsByCourseIds(anySet()))
                .thenReturn(lessonCountsRaw);

        CourseListItemResponse itemResponse = new CourseListItemResponse();
        itemResponse.setId(10L);
        when(courseMapper.toCourseListItemResponse(course)).thenReturn(itemResponse);

        List<CourseListItemResponse> result = dashboardService.getEnrolledCourses(1);

        assertNotNull(result);
        assertEquals(0, result.get(0).getProgressPercentage());
    }

    // ======================== getUserActivitiesByYear ========================

    @Test
    void getUserActivitiesByYear_streaksNull_defaultsToZero() {
        when(activityRepository.findActiveDatesByYear(1, 2025)).thenReturn(Collections.emptyList());
        when(activityRepository.getMaxStreak(1)).thenReturn(null);
        when(activityRepository.getCurrentValidStreak(1)).thenReturn(null);

        UserActivityResponse result = dashboardService.getUserActivitiesByYear(1, 2025);

        assertNotNull(result);
        assertEquals(1, result.getUserId());
        assertEquals(2025, result.getYear());
        assertEquals(0, result.getMaxStreak());
        assertEquals(0, result.getCurrentStreak());
        assertTrue(result.getActiveDates().isEmpty());
    }

    @Test
    void getUserActivitiesByYear_validStreaksAndDates_returnsStreaks() {
        LocalDate date = LocalDate.of(2025, 1, 15);
        when(activityRepository.findActiveDatesByYear(1, 2025)).thenReturn(List.of(date));
        when(activityRepository.getMaxStreak(1)).thenReturn(15);
        when(activityRepository.getCurrentValidStreak(1)).thenReturn(5);

        UserActivityResponse result = dashboardService.getUserActivitiesByYear(1, 2025);

        assertNotNull(result);
        assertEquals(15, result.getMaxStreak());
        assertEquals(5, result.getCurrentStreak());
        assertEquals(1, result.getActiveDates().size());
        assertEquals(date, result.getActiveDates().get(0));
    }

    // ======================== getSubmissionStatistics ========================

    @Test
    void getSubmissionStatistics_noSubmissions_returnsZeroes() {
        when(problemSubmissionRepository.countVerdictsByUserId(1)).thenReturn(Collections.emptyList());

        SubmissionStatisticResponse result = dashboardService.getSubmissionStatistics(1);

        assertNotNull(result);
        assertEquals(0L, result.getTotalSubmissions());
        assertEquals(0L, result.getTotalAccepted());
        assertEquals(0L, result.getTotalWrongAnswer());
        assertEquals(0L, result.getTotalTimeLimitExceeded());
        assertEquals(0L, result.getTotalMemoryLimitExceeded());
    }

    @Test
    void getSubmissionStatistics_hasSubmissions_aggregatesCorrectly() {
        List<Object[]> statsList = new ArrayList<>();
        statsList.add(new Object[]{OjVerdict.ACCEPTED, 10L});
        statsList.add(new Object[]{OjVerdict.WRONG_ANSWER, 5L});
        statsList.add(new Object[]{OjVerdict.TIME_LIMIT_EXCEEDED, 2L});
        statsList.add(new Object[]{OjVerdict.MEMORY_LIMIT_EXCEEDED, 1L});

        when(problemSubmissionRepository.countVerdictsByUserId(1))
                .thenReturn(statsList);

        SubmissionStatisticResponse result = dashboardService.getSubmissionStatistics(1);

        assertNotNull(result);
        assertEquals(18L, result.getTotalSubmissions()); // 10 + 5 + 2 + 1 = 18
        assertEquals(10L, result.getTotalAccepted());
        assertEquals(5L, result.getTotalWrongAnswer());
        assertEquals(2L, result.getTotalTimeLimitExceeded());
        assertEquals(1L, result.getTotalMemoryLimitExceeded());
    }
}
