package com.swp391.coding_platform.service.course;

import com.swp391.coding_platform.dto.request.CourseReviewRequest;
import com.swp391.coding_platform.dto.request.CourseSearchRequest;
import com.swp391.coding_platform.dto.request.CreateCommentRequest;
import com.swp391.coding_platform.dto.response.CourseDetailResponse;
import com.swp391.coding_platform.dto.response.CourseListItemResponse;
import com.swp391.coding_platform.dto.response.CurriculumChapterResponse;
import com.swp391.coding_platform.dto.response.LessonCommentResponse;
import com.swp391.coding_platform.dto.response.PageResponse;
import com.swp391.coding_platform.entity.course.ChapterEntity;
import com.swp391.coding_platform.entity.course.CourseEntity;
import com.swp391.coding_platform.entity.course.EnrollmentEntity;
import com.swp391.coding_platform.entity.course.LessonCommentEntity;
import com.swp391.coding_platform.entity.course.LessonEntity;
import com.swp391.coding_platform.entity.enums.CourseStatus;
import com.swp391.coding_platform.entity.enums.EnrollmentStatus;
import com.swp391.coding_platform.entity.instructor.InstructorEntity;
import com.swp391.coding_platform.entity.progress.CompletedLessonsCountEntity;
import com.swp391.coding_platform.entity.progress.LessonProgressEntity;
import com.swp391.coding_platform.entity.user.UserEntity;
import com.swp391.coding_platform.exception.AppException;
import com.swp391.coding_platform.exception.ErrorCode;
import com.swp391.coding_platform.mapper.CourseMapper;
import com.swp391.coding_platform.repository.course.ChapterRepository;
import com.swp391.coding_platform.repository.course.CourseRepository;
import com.swp391.coding_platform.repository.course.CourseReviewRepository;
import com.swp391.coding_platform.repository.course.LessonCommentRepository;
import com.swp391.coding_platform.repository.course.LessonProblemRepository;
import com.swp391.coding_platform.repository.course.LessonRepository;
import com.swp391.coding_platform.repository.course.QuizRepository;
import com.swp391.coding_platform.repository.course.EnrollmentRepository;
import com.swp391.coding_platform.repository.progress.CompletedLessonCountRepository;
import com.swp391.coding_platform.repository.progress.LessonProgressRepository;
import com.swp391.coding_platform.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;
    @Mock
    private CourseMapper courseMapper;
    @Mock
    private CompletedLessonCountRepository completedLessonCountRepository;
    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private ChapterRepository chapterRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CourseReviewRepository courseReviewRepository;
    @Mock
    private LessonRepository lessonRepository;
    @Mock
    private LessonProgressRepository lessonProgressRepository;
    @Mock
    private LessonCommentRepository lessonCommentRepository;
    @Mock
    private LessonProblemRepository lessonProblemRepository;
    @Mock
    private QuizRepository quizRepository;
    @Mock
    private QuizService quizService;

    @InjectMocks
    private CourseService courseService;

    // ======================== getCourseList ========================

    @Test
    void getCourseList_ShouldReturnPageResponse() {
        CourseEntity course = new CourseEntity();
        course.setId(1L);
        course.setStatus(CourseStatus.APPROVED);

        Page<CourseEntity> page = new PageImpl<>(List.of(course));
        when(courseRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        CourseListItemResponse itemRes = new CourseListItemResponse();
        itemRes.setId(1L);
        when(courseMapper.toCourseListItemResponse(course)).thenReturn(itemRes);
        
        when(enrollmentRepository.findEnrolledCourseIdsByUserIdAndCourseIds(eq(1), anyList(), anyList()))
                .thenReturn(Collections.emptySet());

        PageResponse<CourseListItemResponse> result = courseService.getCourseList(1L, new CourseSearchRequest(), Pageable.unpaged());

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).getId());
    }

    @Test
    void getCourseList_GuestUser_ShouldNotQueryEnrollment() {
        CourseEntity course = new CourseEntity();
        course.setId(1L);
        course.setStatus(CourseStatus.APPROVED);

        Page<CourseEntity> page = new PageImpl<>(List.of(course));
        when(courseRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        CourseListItemResponse itemRes = new CourseListItemResponse();
        itemRes.setId(1L);
        when(courseMapper.toCourseListItemResponse(course)).thenReturn(itemRes);

        // userId=null → no enrollment query
        PageResponse<CourseListItemResponse> result = courseService.getCourseList(null, new CourseSearchRequest(), Pageable.unpaged());

        assertNotNull(result);
        verifyNoInteractions(enrollmentRepository);
        assertEquals(0, result.getContent().get(0).getProgressPercentage());
    }

    @Test
    void getCourseList_UserWithEnrolledCourse_ShouldSetProgressPercentage() {
        CourseEntity course = new CourseEntity();
        course.setId(1L);
        course.setTotalLessons(10);
        course.setStatus(CourseStatus.APPROVED);

        Page<CourseEntity> page = new PageImpl<>(List.of(course));
        when(courseRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        CourseListItemResponse itemRes = new CourseListItemResponse();
        itemRes.setId(1L);
        when(courseMapper.toCourseListItemResponse(course)).thenReturn(itemRes);

        Set<Long> enrolledIds = new HashSet<>();
        enrolledIds.add(1L);
        when(enrollmentRepository.findEnrolledCourseIdsByUserIdAndCourseIds(eq(1), anyList(), anyList()))
                .thenReturn(enrolledIds);

        CompletedLessonsCountEntity countEntity = new CompletedLessonsCountEntity();
        countEntity.setCourse(course);
        countEntity.setCompletedLessonsCount(5);
        when(completedLessonCountRepository.findByUserIdAndCourseIdIn(eq(1), any())).thenReturn(List.of(countEntity));

        PageResponse<CourseListItemResponse> result = courseService.getCourseList(1L, new CourseSearchRequest(), Pageable.unpaged());

        assertNotNull(result);
        assertEquals(50, result.getContent().get(0).getProgressPercentage());
    }

    // ======================== getCourseDetail ========================

    @Test
    void getCourseDetail_CourseNotFound_ShouldThrowException() {
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> courseService.getCourseDetail(1L, 1L));
        assertEquals(ErrorCode.COURSE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void getCourseDetail_CourseNotApprovedAndNotOwnerOrAdmin_ShouldThrowException() {
        UserEntity user = UserEntity.builder().id(99).username("another").build();
        InstructorEntity instructor = InstructorEntity.builder().user(user).build();

        CourseEntity course = CourseEntity.builder()
                .id(1L)
                .status(CourseStatus.DRAFTS)
                .instructor(instructor)
                .build();

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        AppException ex = assertThrows(AppException.class, () -> courseService.getCourseDetail(1L, 1L));
        assertEquals(ErrorCode.COURSE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void getCourseDetail_ApprovedCourse_GuestUser_ShouldReturnWithFalseEnrolled() {
        UserEntity user = UserEntity.builder().id(1).username("owner").build();
        InstructorEntity instructor = InstructorEntity.builder().user(user).build();

        CourseEntity course = CourseEntity.builder()
                .id(1L)
                .status(CourseStatus.APPROVED)
                .instructor(instructor)
                .build();

        CourseDetailResponse detailResponse = new CourseDetailResponse();
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseMapper.toCourseDetailResponse(course)).thenReturn(detailResponse);

        // userId=null (guest user)
        CourseDetailResponse result = courseService.getCourseDetail(null, 1L);

        assertNotNull(result);
        assertFalse(Boolean.TRUE.equals(result.getEnrolled()));
        assertEquals(0, result.getProgressPercentage());
    }

    @Test
    void getCourseDetail_ApprovedCourse_EnrolledUser_ShouldReturnProgress() {
        UserEntity user = UserEntity.builder().id(1).username("owner").build();
        InstructorEntity instructor = InstructorEntity.builder().user(user).build();

        CourseEntity course = CourseEntity.builder()
                .id(1L)
                .status(CourseStatus.APPROVED)
                .totalLessons(10)
                .instructor(instructor)
                .build();

        CourseDetailResponse detailResponse = new CourseDetailResponse();
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseMapper.toCourseDetailResponse(course)).thenReturn(detailResponse);

        when(enrollmentRepository.existsByUserIdAndCourseIdAndStatusIn(anyLong(), anyLong(), anyList()))
                .thenReturn(true);

        CompletedLessonsCountEntity countEntity = new CompletedLessonsCountEntity();
        countEntity.setCompletedLessonsCount(7);
        when(completedLessonCountRepository.getByUserIdAndCourseId(anyInt(), anyLong()))
                .thenReturn(Optional.of(countEntity));

        CourseDetailResponse result = courseService.getCourseDetail(2L, 1L);

        assertNotNull(result);
        assertTrue(Boolean.TRUE.equals(result.getEnrolled()));
        assertEquals(70, result.getProgressPercentage());
    }

    // ======================== getCourseCurriculum ========================

    @Test
    void getCourseCurriculum_CourseNotFound_ShouldThrowException() {
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> courseService.getCourseCurriculum(1L, 1L));
        assertEquals(ErrorCode.COURSE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void getCourseCurriculum_CourseNotApproved_ShouldThrowException() {
        UserEntity user = UserEntity.builder().id(99).username("another").build();
        InstructorEntity instructor = InstructorEntity.builder().user(user).build();

        CourseEntity course = CourseEntity.builder()
                .id(1L)
                .status(CourseStatus.DRAFTS)
                .instructor(instructor)
                .build();

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        AppException ex = assertThrows(AppException.class, () -> courseService.getCourseCurriculum(1L, 1L));
        assertEquals(ErrorCode.COURSE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void getCourseCurriculum_ApprovedCourse_ShouldReturnChapters() {
        UserEntity user = UserEntity.builder().id(1).username("owner").build();
        InstructorEntity instructor = InstructorEntity.builder().user(user).build();

        CourseEntity course = CourseEntity.builder()
                .id(1L)
                .status(CourseStatus.APPROVED)
                .instructor(instructor)
                .build();

        ChapterEntity chapter = new ChapterEntity();
        chapter.setId(1);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(chapterRepository.findByCourseIdOrderByOrderIndexAsc(1L)).thenReturn(List.of(chapter));
        when(courseMapper.toCurriculumChapterResponse(chapter)).thenReturn(new CurriculumChapterResponse());

        List<CurriculumChapterResponse> result = courseService.getCourseCurriculum(null, 1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ======================== upsertCourseReview ========================

    @Test
    void upsertCourseReview_NotEnrolled_ThrowsException() {
        when(enrollmentRepository.existsByUserIdAndCourseIdAndStatusIn(anyLong(), anyLong(), anyList()))
                .thenReturn(false);

        CourseReviewRequest request = new CourseReviewRequest(5, "Nice");

        AppException ex = assertThrows(AppException.class, () -> courseService.upsertCourseReview(1L, 1L, request));
        assertEquals(ErrorCode.NOT_ENROLLED, ex.getErrorCode());
    }

    @Test
    void upsertCourseReview_CourseNotFound_ThrowsException() {
        when(enrollmentRepository.existsByUserIdAndCourseIdAndStatusIn(anyLong(), anyLong(), anyList()))
                .thenReturn(true);
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        CourseReviewRequest request = new CourseReviewRequest(5, "Nice");

        AppException ex = assertThrows(AppException.class, () -> courseService.upsertCourseReview(1L, 1L, request));
        assertEquals(ErrorCode.COURSE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void upsertCourseReview_UserNotFound_ThrowsException() {
        CourseEntity course = CourseEntity.builder().id(1L).build();

        when(enrollmentRepository.existsByUserIdAndCourseIdAndStatusIn(anyLong(), anyLong(), anyList()))
                .thenReturn(true);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        CourseReviewRequest request = new CourseReviewRequest(5, "Nice");

        AppException ex = assertThrows(AppException.class, () -> courseService.upsertCourseReview(1L, 1L, request));
        assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
    }

    // ======================== getLessonComments ========================

    @Test
    void getLessonComments_ShouldReturnListOfComments() {
        UserEntity user = UserEntity.builder().id(1).username("user1").displayname("User One").build();
        LessonCommentEntity comment = LessonCommentEntity.builder()
                .id(1)
                .user(user)
                .content("Great lesson!")
                .replies(Collections.emptyList())
                .build();

        when(lessonCommentRepository.findRootCommentsWithRepliesAndUsers(1)).thenReturn(List.of(comment));

        List<LessonCommentResponse> result = courseService.getLessonComments(1);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Great lesson!", result.get(0).getText());
        assertEquals("User One", result.get(0).getAuthor());
    }

    @Test
    void getLessonComments_NoComments_ShouldReturnEmptyList() {
        when(lessonCommentRepository.findRootCommentsWithRepliesAndUsers(1)).thenReturn(Collections.emptyList());

        List<LessonCommentResponse> result = courseService.getLessonComments(1);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ======================== addLessonComment ========================

    @Test
    void addLessonComment_LessonNotFound_ThrowsException() {
        when(lessonRepository.findById(1)).thenReturn(Optional.empty());

        CreateCommentRequest request = new CreateCommentRequest("Nice lesson!", null);
        AppException ex = assertThrows(AppException.class, () -> courseService.addLessonComment(1, 1, request));
        assertEquals(ErrorCode.LESSON_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void addLessonComment_UserNotFound_ThrowsException() {
        LessonEntity lesson = new LessonEntity();
        lesson.setId(1);
        when(lessonRepository.findById(1)).thenReturn(Optional.of(lesson));
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        CreateCommentRequest request = new CreateCommentRequest("Nice lesson!", null);
        AppException ex = assertThrows(AppException.class, () -> courseService.addLessonComment(1, 1, request));
        assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void addLessonComment_ParentNotFound_ThrowsException() {
        LessonEntity lesson = new LessonEntity();
        lesson.setId(1);
        UserEntity user = UserEntity.builder().id(1).username("user1").build();

        when(lessonRepository.findById(1)).thenReturn(Optional.of(lesson));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(lessonCommentRepository.findById(999)).thenReturn(Optional.empty());

        CreateCommentRequest request = new CreateCommentRequest("Nice lesson!", 999);
        AppException ex = assertThrows(AppException.class, () -> courseService.addLessonComment(1, 1, request));
        assertEquals(ErrorCode.COMMENT_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void addLessonComment_ParentBelongsToDifferentLesson_ThrowsException() {
        LessonEntity lesson = new LessonEntity();
        lesson.setId(1);

        LessonEntity otherLesson = new LessonEntity();
        otherLesson.setId(2);

        UserEntity user = UserEntity.builder().id(1).username("user1").build();

        LessonCommentEntity parent = LessonCommentEntity.builder()
                .id(999)
                .lesson(otherLesson)
                .build();

        when(lessonRepository.findById(1)).thenReturn(Optional.of(lesson));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(lessonCommentRepository.findById(999)).thenReturn(Optional.of(parent));

        CreateCommentRequest request = new CreateCommentRequest("Nice!", 999);
        AppException ex = assertThrows(AppException.class, () -> courseService.addLessonComment(1, 1, request));
        assertEquals(ErrorCode.INVALID_COMMENT_LESSON, ex.getErrorCode());
    }

    @Test
    void addLessonComment_ParentIsAlreadyReply_ThrowsException() {
        LessonEntity lesson = new LessonEntity();
        lesson.setId(1);

        UserEntity user = UserEntity.builder().id(1).username("user1").displayname("User One").build();

        // Grandparent (depth 0)
        LessonCommentEntity grandParent = LessonCommentEntity.builder()
                .id(1)
                .lesson(lesson)
                .parent(null)
                .build();

        // Parent (depth 1 - valid parent)
        LessonCommentEntity existingReply = LessonCommentEntity.builder()
                .id(999)
                .lesson(lesson)
                .parent(grandParent)  // This comment is already a reply → replying to it should fail
                .build();

        when(lessonRepository.findById(1)).thenReturn(Optional.of(lesson));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(lessonCommentRepository.findById(999)).thenReturn(Optional.of(existingReply));

        CreateCommentRequest request = new CreateCommentRequest("Nested reply!", 999);
        AppException ex = assertThrows(AppException.class, () -> courseService.addLessonComment(1, 1, request));
        assertEquals(ErrorCode.INVALID_COMMENT_LEVEL, ex.getErrorCode());
    }

    @Test
    void addLessonComment_TopLevelComment_Success() {
        LessonEntity lesson = new LessonEntity();
        lesson.setId(1);

        UserEntity user = UserEntity.builder().id(1).username("user1").displayname("User One").build();

        LessonCommentEntity savedComment = LessonCommentEntity.builder()
                .id(10)
                .lesson(lesson)
                .user(user)
                .content("Top-level comment")
                .parent(null)
                .replies(Collections.emptyList())
                .build();

        when(lessonRepository.findById(1)).thenReturn(Optional.of(lesson));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(lessonCommentRepository.save(any())).thenReturn(savedComment);

        CreateCommentRequest request = new CreateCommentRequest("Top-level comment", null);
        LessonCommentResponse result = courseService.addLessonComment(1, 1, request);

        assertNotNull(result);
        assertEquals("Top-level comment", result.getText());
        assertEquals("User One", result.getAuthor());
        assertNull(result.getParentId());
    }

    // ======================== completeLesson ========================

    @Test
    void completeLesson_CourseNotFound_ThrowsException() {
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> courseService.completeLesson(1L, 1L, 10));
        assertEquals(ErrorCode.COURSE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void completeLesson_LessonNotFound_ThrowsException() {
        CourseEntity course = CourseEntity.builder().id(1L).build();
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(lessonRepository.findById(10)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> courseService.completeLesson(1L, 1L, 10));
        assertEquals(ErrorCode.LESSON_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void completeLesson_LessonMismatch_ThrowsException() {
        CourseEntity course = CourseEntity.builder().id(1L).build();
        CourseEntity otherCourse = CourseEntity.builder().id(2L).build();
        ChapterEntity chapter = ChapterEntity.builder().course(otherCourse).build();
        LessonEntity lesson = LessonEntity.builder().id(10).chapter(chapter).build();

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(lessonRepository.findById(10)).thenReturn(Optional.of(lesson));

        AppException ex = assertThrows(AppException.class, () -> courseService.completeLesson(1L, 1L, 10));
        assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
    }

    @Test
    void completeLesson_NotEnrolled_ThrowsException() {
        CourseEntity course = CourseEntity.builder().id(1L).build();
        ChapterEntity chapter = ChapterEntity.builder().course(course).build();
        LessonEntity lesson = LessonEntity.builder().id(10).chapter(chapter).build();

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(lessonRepository.findById(10)).thenReturn(Optional.of(lesson));
        when(enrollmentRepository.findEnrollmentWithLock(1, 1L)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> courseService.completeLesson(1L, 1L, 10));
        assertEquals(ErrorCode.NOT_ENROLLED, ex.getErrorCode());
    }

    @Test
    void completeLesson_AlreadyCompleted_ShouldReturnEarlyIdempotent() {
        CourseEntity course = CourseEntity.builder().id(1L).build();
        ChapterEntity chapter = ChapterEntity.builder().course(course).build();
        LessonEntity lesson = LessonEntity.builder().id(10).chapter(chapter).build();
        EnrollmentEntity enrollment = EnrollmentEntity.builder().status(EnrollmentStatus.ACTIVE).build();

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(lessonRepository.findById(10)).thenReturn(Optional.of(lesson));
        when(enrollmentRepository.findEnrollmentWithLock(1, 1L)).thenReturn(Optional.of(enrollment));
        when(lessonProgressRepository.existsByLessonIdAndUserId(10, 1)).thenReturn(true);

        // Should not throw, and should not save progress again
        assertDoesNotThrow(() -> courseService.completeLesson(1L, 1L, 10));
        verify(lessonProgressRepository, never()).save(any());
    }

    @Test
    void completeLesson_Success_FirstTime_ShouldCreateProgressRecord() {
        CourseEntity course = CourseEntity.builder().id(1L).totalLessons(2).build();
        ChapterEntity chapter = ChapterEntity.builder().course(course).build();
        LessonEntity lesson = LessonEntity.builder().id(10).chapter(chapter).build();
        EnrollmentEntity enrollment = EnrollmentEntity.builder().status(EnrollmentStatus.ACTIVE).build();
        UserEntity user = UserEntity.builder().id(1).build();

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(lessonRepository.findById(10)).thenReturn(Optional.of(lesson));
        when(enrollmentRepository.findEnrollmentWithLock(1, 1L)).thenReturn(Optional.of(enrollment));
        when(lessonProgressRepository.existsByLessonIdAndUserId(10, 1)).thenReturn(false);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        CompletedLessonsCountEntity countEntity = CompletedLessonsCountEntity.builder()
                .completedLessonsCount(0)
                .course(course)
                .user(user)
                .build();
        when(completedLessonCountRepository.getByUserIdAndCourseId(1, 1L)).thenReturn(Optional.of(countEntity));

        assertDoesNotThrow(() -> courseService.completeLesson(1L, 1L, 10));

        verify(lessonProgressRepository).save(any(LessonProgressEntity.class));
        verify(completedLessonCountRepository).save(any(CompletedLessonsCountEntity.class));
        assertEquals(1, countEntity.getCompletedLessonsCount());
        // Only 1 out of 2 lessons done, enrollment should still be ACTIVE
        assertEquals(EnrollmentStatus.ACTIVE, enrollment.getStatus());
    }

    @Test
    void completeLesson_CompletesAllLessons_ShouldUpdateEnrollmentToCompleted() {
        CourseEntity course = CourseEntity.builder().id(1L).totalLessons(1).build();
        ChapterEntity chapter = ChapterEntity.builder().course(course).build();
        LessonEntity lesson = LessonEntity.builder().id(10).chapter(chapter).build();
        EnrollmentEntity enrollment = EnrollmentEntity.builder().status(EnrollmentStatus.ACTIVE).build();
        UserEntity user = UserEntity.builder().id(1).build();

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(lessonRepository.findById(10)).thenReturn(Optional.of(lesson));
        when(enrollmentRepository.findEnrollmentWithLock(1, 1L)).thenReturn(Optional.of(enrollment));
        when(lessonProgressRepository.existsByLessonIdAndUserId(10, 1)).thenReturn(false);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        CompletedLessonsCountEntity countEntity = CompletedLessonsCountEntity.builder()
                .completedLessonsCount(0)
                .course(course)
                .user(user)
                .build();
        when(completedLessonCountRepository.getByUserIdAndCourseId(1, 1L)).thenReturn(Optional.of(countEntity));

        assertDoesNotThrow(() -> courseService.completeLesson(1L, 1L, 10));

        // 1 out of 1 lessons completed → enrollment should be COMPLETED
        assertEquals(EnrollmentStatus.COMPLETED, enrollment.getStatus());
        verify(enrollmentRepository).save(enrollment);
    }
}
