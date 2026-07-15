package com.swp391.coding_platform.service.moderation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swp391.coding_platform.dto.moderation.CourseModerationPayload;
import com.swp391.coding_platform.dto.moderation.ModerationResult;
import com.swp391.coding_platform.entity.course.*;
import com.swp391.coding_platform.entity.enums.CourseStatus;
import com.swp391.coding_platform.entity.enums.LessonStatus;
import com.swp391.coding_platform.repository.course.ChapterRepository;
import com.swp391.coding_platform.repository.course.CourseModerationReportRepository;
import com.swp391.coding_platform.repository.course.CourseRepository;
import com.swp391.coding_platform.repository.course.LessonRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseModerationServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private ChapterRepository chapterRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private CourseModerationReportRepository reportRepository;

    @Mock
    private VideoTranscriptionService videoTranscriptionService;

    @Mock
    private AiEvaluationService aiEvaluationService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CourseModerationService moderationService;

    @Test
    void processFullCourse_CourseNotFound_ShouldDoNothing() {
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        moderationService.processFullCourse(1L);

        verify(reportRepository, never()).findByCourseId(any());
    }

    @Test
    void processFullCourse_EmptyChapters_ShouldPassAI() throws Exception {
        CourseEntity course = new CourseEntity();
        course.setId(1L);
        course.setTitle("Title");

        CourseModerationReportEntity report = new CourseModerationReportEntity();

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(reportRepository.findByCourseId(1L)).thenReturn(Optional.of(report));
        when(chapterRepository.findByCourseIdOrderByOrderIndexAsc(1L)).thenReturn(Collections.emptyList());

        ModerationResult result = ModerationResult.builder().isClean(true).build();
        when(aiEvaluationService.evaluateCourse(any(CourseModerationPayload.class))).thenReturn(result);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        moderationService.processFullCourse(1L);

        assertEquals(CourseStatus.PENDING_ADMIN, course.getStatus());
        assertEquals("PASSED_AI_WAITING_ADMIN", report.getStatus());
        verify(courseRepository).save(course);
        verify(reportRepository).save(report);
    }

    @Test
    void processFullCourse_PrecheckViolation_ShouldReject() {
        CourseEntity course = CourseEntity.builder().id(1L).title("Algorithms").build();
        CourseModerationReportEntity report = new CourseModerationReportEntity();
        
        // Setup a lesson without video URL and without quizzes -> video ratio = 0.0, quiz ratio = 0.0
        LessonEntity lesson = LessonEntity.builder().id(10).title("Lesson 1").build();
        ChapterEntity chapter = ChapterEntity.builder().lessons(List.of(lesson)).build();

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(reportRepository.findByCourseId(1L)).thenReturn(Optional.of(report));
        when(chapterRepository.findByCourseIdOrderByOrderIndexAsc(1L)).thenReturn(List.of(chapter));

        moderationService.processFullCourse(1L);

        assertEquals(CourseStatus.REJECTED, course.getStatus());
        assertEquals("REJECTED_PRECHECK", report.getStatus());
        verify(courseRepository).save(course);
    }

    @Test
    void processFullCourse_EvaluateClean_WithTranscriptions() throws Exception {
        CourseEntity course = CourseEntity.builder().id(1L).title("Advanced Java").build();
        CourseModerationReportEntity report = new CourseModerationReportEntity();

        // 1 lesson with video and 1 with quiz -> video ratio = 0.5, quiz ratio = 0.5 (passes pre-check)
        LessonEntity lesson1 = LessonEntity.builder().id(10).title("Lesson 1").videoUrl("http://video.com").build();
        QuizEntity quiz = QuizEntity.builder().title("Q1").questions(Collections.emptyList()).build();
        LessonEntity lesson2 = LessonEntity.builder().id(20).title("Lesson 2").quizzes(List.of(quiz)).build();
        
        ChapterEntity chapter = ChapterEntity.builder().lessons(List.of(lesson1, lesson2)).build();

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(reportRepository.findByCourseId(1L)).thenReturn(Optional.of(report));
        when(chapterRepository.findByCourseIdOrderByOrderIndexAsc(1L)).thenReturn(List.of(chapter));
        when(videoTranscriptionService.transcribeVideoAsync(1L, 10L, "http://video.com"))
                .thenReturn(CompletableFuture.completedFuture("Java transcript"));

        ModerationResult aiResult = ModerationResult.builder().isClean(true).build();
        when(aiEvaluationService.evaluateCourse(any(CourseModerationPayload.class))).thenReturn(aiResult);
        when(objectMapper.writeValueAsString(aiResult)).thenReturn("{\"isClean\": true}");

        moderationService.processFullCourse(1L);

        assertEquals(CourseStatus.PENDING_ADMIN, course.getStatus());
        assertEquals("PASSED_AI_WAITING_ADMIN", report.getStatus());
        verify(lessonRepository, times(2)).save(any(LessonEntity.class));
        verify(courseRepository).save(course);
        verify(reportRepository).save(report);
    }

    @Test
    void processSingleLessonUpdate_LessonNotFound_ShouldDoNothing() {
        when(lessonRepository.findById(1)).thenReturn(Optional.empty());

        moderationService.processSingleLessonUpdate(1L);

        verify(aiEvaluationService, never()).evaluateSingleLesson(any());
    }

    @Test
    void processSingleLessonUpdate_Clean() throws Exception {
        CourseEntity course = CourseEntity.builder().id(1L).build();
        ChapterEntity chapter = ChapterEntity.builder().course(course).build();
        LessonEntity lesson = LessonEntity.builder().id(10).title("Inheritance").chapter(chapter).build();

        CourseModerationReportEntity report = CourseModerationReportEntity.builder().courseId(1L).reportJson("{}").build();

        when(lessonRepository.findById(10)).thenReturn(Optional.of(lesson));
        when(reportRepository.findByCourseId(1L)).thenReturn(Optional.of(report));

        ModerationResult result = ModerationResult.builder().isClean(true).build();
        when(aiEvaluationService.evaluateSingleLesson(any())).thenReturn(result);
        when(objectMapper.readValue("{}", ModerationResult.class)).thenReturn(result);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        moderationService.processSingleLessonUpdate(10L);

        assertEquals(LessonStatus.ACTIVE, lesson.getStatus());
        verify(lessonRepository).save(lesson);
        verify(reportRepository).save(report);
    }

    @Test
    void processSingleLessonUpdate_Dirty() throws Exception {
        CourseEntity course = CourseEntity.builder().id(1L).build();
        ChapterEntity chapter = ChapterEntity.builder().course(course).build();
        LessonEntity lesson = LessonEntity.builder().id(10).title("Inheritance").chapter(chapter).build();

        CourseModerationReportEntity report = CourseModerationReportEntity.builder().courseId(1L).reportJson("").build();

        when(lessonRepository.findById(10)).thenReturn(Optional.of(lesson));
        when(reportRepository.findByCourseId(1L)).thenReturn(Optional.of(report));

        ModerationResult result = ModerationResult.builder().isClean(false).build();
        when(aiEvaluationService.evaluateSingleLesson(any())).thenReturn(result);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"isClean\": false}");

        moderationService.processSingleLessonUpdate(10L);

        assertEquals(LessonStatus.INACTIVE, lesson.getStatus());
        verify(lessonRepository).save(lesson);
        verify(reportRepository).save(report);
    }
}
