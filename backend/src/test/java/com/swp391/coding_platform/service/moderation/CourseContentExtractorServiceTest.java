package com.swp391.coding_platform.service.moderation;

import com.swp391.coding_platform.entity.course.*;
import com.swp391.coding_platform.repository.course.ChapterRepository;
import com.swp391.coding_platform.repository.course.QuizQuestionRepository;
import com.swp391.coding_platform.repository.course.QuizRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseContentExtractorServiceTest {

    @Mock
    private ChapterRepository chapterRepository;

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuizQuestionRepository quizQuestionRepository;

    @InjectMocks
    private CourseContentExtractorService courseContentExtractorService;

    @Test
    void testExtractMetadata() {
        CourseEntity course = new CourseEntity();
        course.setId(1L);
        course.setTitle("Title");
        course.setShortDescription("Short");
        course.setLongDescription("Long");
        course.setWhatYouLearn("Learn");
        course.setPrerequisites("Pre");
        course.setTargetAudience("Audience");

        String result = courseContentExtractorService.extractMetadata(course);
        assertTrue(result.contains("Tiêu đề: Title"));
        assertTrue(result.contains("Mô tả ngắn: Short"));
        assertTrue(result.contains("Mô tả chi tiết: Long"));
    }

    @Test
    void testExtractQuizContent_Empty() {
        Long courseId = 1L;
        when(chapterRepository.findByCourseIdOrderByOrderIndexAsc(courseId)).thenReturn(List.of());

        String result = courseContentExtractorService.extractQuizContent(courseId);
        assertEquals("", result);
    }

    @Test
    void testExtractQuizContent_Success() {
        Long courseId = 1L;

        ChapterEntity chapter = new ChapterEntity();
        LessonEntity lesson = new LessonEntity();
        lesson.setId(10);
        lesson.setTitle("Lesson 1");
        chapter.setLessons(List.of(lesson));

        QuizEntity quiz = new QuizEntity();
        quiz.setId(100);
        quiz.setTitle("Quiz 1");

        QuizQuestionEntity question = new QuizQuestionEntity();
        question.setContent("Q1");
        
        QuizOptionEntity option = new QuizOptionEntity();
        option.setContent("Option 1");
        option.setIsCorrect(true);
        question.setOptions(List.of(option));

        when(chapterRepository.findByCourseIdOrderByOrderIndexAsc(courseId)).thenReturn(List.of(chapter));
        when(quizRepository.findByLessonId(10)).thenReturn(Optional.of(quiz));
        when(quizQuestionRepository.findByQuizIdWithOptions(100)).thenReturn(List.of(question));

        String result = courseContentExtractorService.extractQuizContent(courseId);

        assertTrue(result.contains("### Lesson: Lesson 1"));
        assertTrue(result.contains("Quiz Title: Quiz 1"));
        assertTrue(result.contains("Câu hỏi 1: Q1"));
        assertTrue(result.contains("Option 1 [ĐÁP ÁN ĐÚNG]"));
    }

    @Test
    void testExtractTextFromPdf_EmptyUrl() {
        String result = courseContentExtractorService.extractTextFromPdf("");
        assertEquals("", result);
    }

    @Test
    void testExtractTextFromPdf_Exception() {
        String result = courseContentExtractorService.extractTextFromPdf("invalid-url");
        assertTrue(result.startsWith("[LỖI TRÍCH XUẤT PDF:"));
    }

    @Test
    void testExtractTextFromDocx_EmptyUrl() {
        String result = courseContentExtractorService.extractTextFromDocx("");
        assertEquals("", result);
    }

    @Test
    void testExtractTextFromDocx_Exception() {
        String result = courseContentExtractorService.extractTextFromDocx("invalid-url");
        assertTrue(result.startsWith("[LỖI TRÍCH XUẤT DOCX:"));
    }
}
