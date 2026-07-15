package com.swp391.coding_platform.service.instructor;

import com.swp391.coding_platform.dto.request.InstructorCourseCreateRequest;
import com.swp391.coding_platform.dto.request.InstructorCourseUpdateRequest;
import com.swp391.coding_platform.dto.request.TestcaseGeneratorRequest;
import com.swp391.coding_platform.dto.judge0.Judge0CallbackPayload;
import com.swp391.coding_platform.dto.response.*;
import com.swp391.coding_platform.entity.category.CategoryEntity;
import com.swp391.coding_platform.entity.course.*;
import com.swp391.coding_platform.entity.enums.*;
import com.swp391.coding_platform.entity.instructor.InstructorEntity;
import com.swp391.coding_platform.entity.progress.CompletedLessonsCountEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import com.swp391.coding_platform.exception.AppException;
import com.swp391.coding_platform.exception.ErrorCode;
import com.swp391.coding_platform.mapper.CourseMapper;
import com.swp391.coding_platform.repository.course.ChapterRepository;
import com.swp391.coding_platform.repository.course.CourseRepository;
import com.swp391.coding_platform.repository.course.EnrollmentRepository;
import com.swp391.coding_platform.repository.course.LessonRepository;
import com.swp391.coding_platform.repository.instructor.InstructorRepository;
import com.swp391.coding_platform.repository.category.CategoryRepository;
import com.swp391.coding_platform.repository.progress.CompletedLessonCountRepository;
import com.swp391.coding_platform.service.judge0.Judge0ClientService;
import com.swp391.coding_platform.service.moderation.CourseModerationListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstructorCourseServiceTest {

    @Mock
    private InstructorRepository instructorRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private ChapterRepository chapterRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private CompletedLessonCountRepository completedLessonCountRepository;

    @Mock
    private Judge0ClientService judge0ClientService;

    @Mock
    private CourseModerationListener courseModerationListener;

    @InjectMocks
    private InstructorCourseService instructorCourseService;

    @Test
    void getInstructor_NotFound_ThrowsException() {
        when(instructorRepository.findByUserId(1)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> instructorCourseService.getCourses(1));
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void getInstructor_Suspended_ThrowsException() {
        InstructorEntity instructor = InstructorEntity.builder()
                .id(1)
                .status(InstructorStatus.SUSPENDED)
                .build();

        when(instructorRepository.findByUserId(1)).thenReturn(Optional.of(instructor));

        AppException ex = assertThrows(AppException.class, () -> instructorCourseService.getCourses(1));
        assertEquals(ErrorCode.ACCESS_DENIED, ex.getErrorCode());
    }

    @Test
    void getCourseDetail_ShouldReturnDetail() {
        InstructorEntity instructor = new InstructorEntity();
        instructor.setId(1);
        instructor.setStatus(InstructorStatus.ACTIVE);

        CourseEntity course = new CourseEntity();
        course.setId(10L);

        when(instructorRepository.findByUserId(1)).thenReturn(Optional.of(instructor));
        when(courseRepository.findByIdAndInstructorId(10L, 1)).thenReturn(Optional.of(course));

        InstructorCourseDetailResponse response = new InstructorCourseDetailResponse();
        response.setId(10L);
        when(courseMapper.toInstructorCourseDetailResponse(course)).thenReturn(response);

        InstructorCourseDetailResponse res = instructorCourseService.getCourseDetail(1, 10L);

        assertNotNull(res);
        assertEquals(10L, res.getId());
    }

    @Test
    void getCourseDetail_NotFound_ThrowsException() {
        InstructorEntity instructor = InstructorEntity.builder().id(1).status(InstructorStatus.ACTIVE).build();
        when(instructorRepository.findByUserId(1)).thenReturn(Optional.of(instructor));
        when(courseRepository.findByIdAndInstructorId(10L, 1)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> instructorCourseService.getCourseDetail(1, 10L));
        assertEquals(ErrorCode.COURSE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void createCourse_ShouldSaveAndReturn() {
        InstructorEntity instructor = new InstructorEntity();
        instructor.setId(1);
        instructor.setStatus(InstructorStatus.ACTIVE);

        when(instructorRepository.findByUserId(1)).thenReturn(Optional.of(instructor));
        
        CourseEntity savedCourse = new CourseEntity();
        savedCourse.setId(10L);
        savedCourse.setTitle("New Course");
        savedCourse.setPrice(BigDecimal.ZERO);
        savedCourse.setType("FREE");

        when(courseRepository.save(any())).thenReturn(savedCourse);

        InstructorCourseCreateRequest req = new InstructorCourseCreateRequest();
        req.setTitle("New Course");
        req.setIsFree(true);

        InstructorCourseResponse res = instructorCourseService.createCourse(1, req);

        assertNotNull(res);
        assertEquals("10", res.getId());
        assertEquals("New Course", res.getTitle());
    }

    @Test
    void createCourse_withVariousInputs_ShouldComputeCorrectDefaults() {
        InstructorEntity instructor = new InstructorEntity();
        instructor.setId(1);
        instructor.setStatus(InstructorStatus.ACTIVE);

        when(instructorRepository.findByUserId(1)).thenReturn(Optional.of(instructor));
        when(categoryRepository.findAllById(anyList())).thenReturn(List.of(new CategoryEntity()));
        
        CourseEntity savedCourse = new CourseEntity();
        savedCourse.setId(11L);
        savedCourse.setTitle("Paid Course");
        savedCourse.setPrice(BigDecimal.valueOf(100));
        savedCourse.setType("PAID");

        when(courseRepository.save(any(CourseEntity.class))).thenAnswer(i -> {
            CourseEntity argument = i.getArgument(0);
            argument.setId(11L);
            return argument;
        });

        InstructorCourseCreateRequest req = new InstructorCourseCreateRequest();
        req.setTitle("Paid Course");
        req.setShortDescription("short");
        req.setLongDescription("long");
        req.setThumbnailUrl(""); // should trigger fallback to placeholder
        req.setIsFree(false);
        req.setPrice(BigDecimal.valueOf(100));
        req.setWhatYouLearn(List.of("A", "B"));
        req.setCategoryIds(List.of(1, 2));

        InstructorCourseResponse res = instructorCourseService.createCourse(1, req);

        assertNotNull(res);
        assertEquals("11", res.getId());
        verify(courseRepository).save(argThat(course -> {
            assertEquals("https://placehold.co/600x400/2563eb/ffffff?text=Course", course.getThumbnailUrl());
            assertEquals("long", course.getLongDescription());
            assertEquals("A#B", course.getWhatYouLearn());
            assertEquals("PAID", course.getType());
            return true;
        }));
    }

    @Test
    void getCourses_ShouldReturnList() {
        InstructorEntity instructor = InstructorEntity.builder().id(1).status(InstructorStatus.ACTIVE).build();
        CourseEntity course = CourseEntity.builder()
                .id(10L)
                .title("Course One")
                .status(CourseStatus.APPROVED)
                .type("DATABASE")
                .price(BigDecimal.valueOf(200000))
                .totalEnrolled(10)
                .averageRating(4.5)
                .totalReviews(5)
                .build();

        when(instructorRepository.findByUserId(1)).thenReturn(Optional.of(instructor));
        when(courseRepository.findByInstructorId(1)).thenReturn(List.of(course));

        List<InstructorCourseResponse> list = instructorCourseService.getCourses(1);

        assertNotNull(list);
        assertEquals(1, list.size());
        InstructorCourseResponse response = list.get(0);
        assertEquals("10", response.getId());
        assertEquals("published", response.getStatus());
        assertEquals("database", response.getIcon());
    }

    @Test
    void getCourses_mappingStatusAndIcons() {
        InstructorEntity instructor = InstructorEntity.builder().id(1).status(InstructorStatus.ACTIVE).build();
        
        CourseEntity course1 = CourseEntity.builder()
                .id(1L)
                .title("Course One")
                .status(CourseStatus.PENDING_AI)
                .type("DEVOPS")
                .build();

        CourseEntity course2 = CourseEntity.builder()
                .id(2L)
                .title("Course Two")
                .status(CourseStatus.REJECTED)
                .type("DATA_SCIENCE")
                .build();

        when(instructorRepository.findByUserId(1)).thenReturn(Optional.of(instructor));
        when(courseRepository.findByInstructorId(1)).thenReturn(List.of(course1, course2));

        List<InstructorCourseResponse> list = instructorCourseService.getCourses(1);

        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals("review", list.get(0).getStatus());
        assertEquals("dns", list.get(0).getIcon());
        
        assertEquals("rejected", list.get(1).getStatus());
        assertEquals("analytics", list.get(1).getIcon());
    }

    @Test
    void submitCourseForReview_ShouldUpdateStatusAndSendToRabbit() {
        InstructorEntity instructor = new InstructorEntity();
        instructor.setId(1);
        instructor.setStatus(InstructorStatus.ACTIVE);

        CourseEntity course = new CourseEntity();
        course.setId(10L);
        course.setStatus(CourseStatus.DRAFTS);

        when(instructorRepository.findByUserId(1)).thenReturn(Optional.of(instructor));
        when(courseRepository.findByIdAndInstructorId(10L, 1)).thenReturn(Optional.of(course));

        instructorCourseService.submitCourseForReview(1, 10L);

        assertEquals(CourseStatus.PENDING_AI, course.getStatus());
        verify(courseRepository).save(course);
        verify(rabbitTemplate, atLeastOnce()).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void submitCourseForReview_withLessonsAndChapters_ShouldSetLessonsActive() {
        InstructorEntity instructor = new InstructorEntity();
        instructor.setId(1);
        instructor.setStatus(InstructorStatus.ACTIVE);

        LessonEntity lesson = new LessonEntity();
        lesson.setId(1);
        lesson.setStatus(null); // Should be activated to ACTIVE

        ChapterEntity chapter = new ChapterEntity();
        chapter.setId(1);
        chapter.setLessons(List.of(lesson));

        CourseEntity course = new CourseEntity();
        course.setId(10L);
        course.setStatus(CourseStatus.DRAFTS);
        course.setChapters(List.of(chapter));

        when(instructorRepository.findByUserId(1)).thenReturn(Optional.of(instructor));
        when(courseRepository.findByIdAndInstructorId(10L, 1)).thenReturn(Optional.of(course));

        instructorCourseService.submitCourseForReview(1, 10L);

        assertEquals(LessonStatus.ACTIVE, lesson.getStatus());
    }

    @Test
    void submitCourseForReview_RabbitFallback_ShouldExecuteCompletableFuture() {
        InstructorEntity instructor = new InstructorEntity();
        instructor.setId(1);
        instructor.setStatus(InstructorStatus.ACTIVE);

        CourseEntity course = new CourseEntity();
        course.setId(10L);
        course.setStatus(CourseStatus.DRAFTS);

        when(instructorRepository.findByUserId(1)).thenReturn(Optional.of(instructor));
        when(courseRepository.findByIdAndInstructorId(10L, 1)).thenReturn(Optional.of(course));

        // Mock rabbit to throw exception to trigger fallback
        doThrow(new RuntimeException("Rabbit down")).when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        // Run submit (will swallow exception internally and run moderation queue listener process)
        assertDoesNotThrow(() -> instructorCourseService.submitCourseForReview(1, 10L));
    }

    @Test
    void submitCourseForReview_NotFound_ThrowsException() {
        InstructorEntity instructor = InstructorEntity.builder().id(1).status(InstructorStatus.ACTIVE).build();
        when(instructorRepository.findByUserId(1)).thenReturn(Optional.of(instructor));
        when(courseRepository.findByIdAndInstructorId(10L, 1)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> instructorCourseService.submitCourseForReview(1, 10L));
        assertEquals(ErrorCode.COURSE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void submitCourseForReview_InvalidStatus_ThrowsException() {
        InstructorEntity instructor = InstructorEntity.builder().id(1).status(InstructorStatus.ACTIVE).build();
        CourseEntity course = CourseEntity.builder().id(10L).status(CourseStatus.APPROVED).build();

        when(instructorRepository.findByUserId(1)).thenReturn(Optional.of(instructor));
        when(courseRepository.findByIdAndInstructorId(10L, 1)).thenReturn(Optional.of(course));

        AppException ex = assertThrows(AppException.class, () -> instructorCourseService.submitCourseForReview(1, 10L));
        assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
    }

    @Test
    void triggerModerationForNewCourse_SuccessAndFallback() {
        // Test Success (Rabbit works)
        assertDoesNotThrow(() -> instructorCourseService.triggerModerationForNewCourse(5L));
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), any(Object.class));

        // Test Fallback (Rabbit throws exception)
        doThrow(new RuntimeException("Rabbit MQ error")).when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));
        assertDoesNotThrow(() -> instructorCourseService.triggerModerationForNewCourse(5L));
    }

    @Test
    void updateCourse_Success() {
        InstructorEntity instructor = InstructorEntity.builder().id(1).status(InstructorStatus.ACTIVE).build();
        CourseEntity course = CourseEntity.builder()
                .id(10L)
                .title("Old Title")
                .status(CourseStatus.DRAFTS)
                .type("FREE")
                .price(BigDecimal.ZERO)
                .chapters(new ArrayList<>())
                .build();

        when(instructorRepository.findByUserId(1)).thenReturn(Optional.of(instructor));
        when(courseRepository.findByIdAndInstructorId(10L, 1)).thenReturn(Optional.of(course));
        when(courseRepository.save(any(CourseEntity.class))).thenAnswer(i -> i.getArgument(0));

        InstructorCourseUpdateRequest req = new InstructorCourseUpdateRequest();
        req.setTitle("Updated Title");
        req.setIsFree(false);
        req.setPrice(BigDecimal.valueOf(150000));
        req.setChapters(Collections.emptyList());

        InstructorCourseResponse res = instructorCourseService.updateCourse(1, 10L, req);

        assertNotNull(res);
        assertEquals("Updated Title", course.getTitle());
        assertEquals(BigDecimal.valueOf(150000), course.getPrice());
        assertEquals("PAID", course.getType());
    }

    @Test
    void getCourseStatistics_Success() {
        InstructorEntity instructor = InstructorEntity.builder().id(1).status(InstructorStatus.ACTIVE).build();
        CourseEntity course = CourseEntity.builder()
                .id(10L)
                .price(BigDecimal.valueOf(100000))
                .totalEnrolled(5)
                .totalLessons(10)
                .averageRating(4.8)
                .totalReviews(2)
                .build();

        UserEntity student = UserEntity.builder().id(100).displayname("Alice").email("alice@test.com").build();
        EnrollmentEntity enrollment = EnrollmentEntity.builder().user(student).build();
        CompletedLessonsCountEntity progress = CompletedLessonsCountEntity.builder()
                .user(student)
                .completedLessonsCount(5)
                .build();

        when(instructorRepository.findByUserId(1)).thenReturn(Optional.of(instructor));
        when(courseRepository.findByIdAndInstructorId(10L, 1)).thenReturn(Optional.of(course));
        when(enrollmentRepository.findByCourseId(10L)).thenReturn(List.of(enrollment));
        when(completedLessonCountRepository.findByCourseId(10L)).thenReturn(List.of(progress));

        CourseStatisticResponse res = instructorCourseService.getCourseStatistics(1, 10L);

        assertNotNull(res);
        assertEquals(5, res.getTotalEnrollments());
        assertEquals(0, BigDecimal.valueOf(500000).compareTo(res.getTotalRevenue()));
        assertEquals(50.0, res.getAverageCompletionRate());
        assertEquals(1, res.getStudents().size());
        assertEquals("Alice", res.getStudents().get(0).getFullName());
    }

    @Test
    void getCourseStatistics_EdgeCases() {
        InstructorEntity instructor = InstructorEntity.builder().id(1).status(InstructorStatus.ACTIVE).build();
        
        // Case 1: totalLessons is null/0
        CourseEntity course = CourseEntity.builder()
                .id(10L)
                .price(BigDecimal.valueOf(100000))
                .totalEnrolled(5)
                .totalLessons(0) // 0 lessons
                .build();

        UserEntity student = UserEntity.builder().id(100).build();
        EnrollmentEntity enrollment = EnrollmentEntity.builder().user(student).build();
        CompletedLessonsCountEntity progress = CompletedLessonsCountEntity.builder()
                .user(student)
                .completedLessonsCount(10) // progress check
                .build();

        when(instructorRepository.findByUserId(1)).thenReturn(Optional.of(instructor));
        when(courseRepository.findByIdAndInstructorId(10L, 1)).thenReturn(Optional.of(course));
        when(enrollmentRepository.findByCourseId(10L)).thenReturn(List.of(enrollment));
        when(completedLessonCountRepository.findByCourseId(10L)).thenReturn(List.of(progress));

        CourseStatisticResponse res = instructorCourseService.getCourseStatistics(1, 10L);
        assertNotNull(res);
        // Total completion caps check
        assertTrue(res.getAverageCompletionRate() <= 100.0);

        // Case 2: enrollment list is empty (should not division by zero and return avg 0.0)
        when(enrollmentRepository.findByCourseId(10L)).thenReturn(Collections.emptyList());
        CourseStatisticResponse resEmpty = instructorCourseService.getCourseStatistics(1, 10L);
        assertNotNull(resEmpty);
        assertEquals(0.0, resEmpty.getAverageCompletionRate());
    }

    @Test
    void generateTestcases_Success() {
        TestcaseGeneratorRequest req = new TestcaseGeneratorRequest("python", "print('INPUT:\\n1\\nOUTPUT:\\n2')");
        Judge0CallbackPayload payload = new Judge0CallbackPayload();
        Judge0CallbackPayload.Judge0Status status = new Judge0CallbackPayload.Judge0Status();
        status.setId(3);
        payload.setStatus(status);
        payload.setStdout("INPUT:\n1\nOUTPUT:\n2\n---TESTCASE---");

        when(judge0ClientService.submitSynchronous(eq(71), anyString())).thenReturn(payload);

        List<InstructorCourseUpdateRequest.TestcaseDto> list = instructorCourseService.generateTestcases(req);

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("1", list.get(0).getInput());
        assertEquals("2", list.get(0).getOutput());
    }

    @Test
    void generateTestcases_UnsupportedLanguage_ThrowsException() {
        TestcaseGeneratorRequest req = new TestcaseGeneratorRequest("unsupported_language", "code");
        assertThrows(RuntimeException.class, () -> instructorCourseService.generateTestcases(req));
    }

    @Test
    void generateTestcases_CompilationFailure_ThrowsException() {
        TestcaseGeneratorRequest req = new TestcaseGeneratorRequest("python", "invalid python");
        Judge0CallbackPayload payload = new Judge0CallbackPayload();
        Judge0CallbackPayload.Judge0Status status = new Judge0CallbackPayload.Judge0Status();
        status.setId(4); // Non-3 status (failure)
        payload.setStatus(status);
        payload.setCompileOutput("Compile error occurred");
        payload.setStderr("Syntax error");

        when(judge0ClientService.submitSynchronous(anyInt(), anyString())).thenReturn(payload);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> instructorCourseService.generateTestcases(req));
        assertTrue(ex.getMessage().contains("Compile error occurred") || ex.getMessage().contains("Syntax error"));
    }
}
