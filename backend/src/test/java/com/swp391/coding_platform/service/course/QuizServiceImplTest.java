package com.swp391.coding_platform.service.course;

import com.swp391.coding_platform.dto.request.QuizSubmitRequest;
import com.swp391.coding_platform.dto.response.*;
import com.swp391.coding_platform.entity.course.*;
import com.swp391.coding_platform.entity.enums.LessonStatus;
import com.swp391.coding_platform.exception.AppException;
import com.swp391.coding_platform.exception.ErrorCode;
import com.swp391.coding_platform.mapper.QuizMapper;
import com.swp391.coding_platform.repository.course.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QuizServiceImplTest {

    @Mock
    private QuizRepository quizRepository;
    @Mock
    private QuizQuestionRepository quizQuestionRepository;
    @Mock
    private QuizAttemptRepository quizAttemptRepository;
    @Mock
    private QuizAttemptAnswerRepository quizAttemptAnswerRepository;
    @Mock
    private QuizMapper quizMapper;

    @InjectMocks
    private QuizServiceImpl quizService;

    @Test
    void getQuizDetailByLessonId_NotFound_ThrowsException() {
        when(quizRepository.findByLessonId(1)).thenReturn(Optional.empty());

        assertThrows(AppException.class, () -> quizService.getQuizDetailByLessonId(1, 1));
    }

    @Test
    void getQuizDetailByLessonId_InactiveLesson_ThrowsException() {
        QuizEntity quiz = new QuizEntity();
        LessonEntity lesson = new LessonEntity();
        lesson.setStatus(LessonStatus.INACTIVE);
        quiz.setLesson(lesson);

        when(quizRepository.findByLessonId(1)).thenReturn(Optional.of(quiz));

        assertThrows(AppException.class, () -> quizService.getQuizDetailByLessonId(1, 1));
    }

    @Test
    void getQuizDetailByLessonId_Success_NotSubmitted() {
        QuizEntity quiz = new QuizEntity();
        quiz.setId(1);
        quiz.setTitle("Test Quiz");

        when(quizRepository.findByLessonId(1)).thenReturn(Optional.of(quiz));
        when(quizQuestionRepository.findByQuizIdWithOptions(1)).thenReturn(Collections.emptyList());
        when(quizAttemptRepository.findTopByUserIdAndQuizIdOrderBySubmittedAtDesc(1, 1)).thenReturn(Optional.empty());

        QuizDetailResponse response = quizService.getQuizDetailByLessonId(1, 1);

        assertNotNull(response);
        assertFalse(response.getSubmitted());
        assertEquals("Test Quiz", response.getTitle());
    }

    @Test
    void getQuizDetailByLessonId_Success_AlreadySubmitted() {
        QuizEntity quiz = QuizEntity.builder().id(1).title("Math Quiz").build();

        QuizOptionEntity option = QuizOptionEntity.builder().id(10).content("Option A").isCorrect(true).build();
        QuizQuestionEntity question = QuizQuestionEntity.builder().id(5).content("Q1").options(List.of(option)).build();

        QuizAttemptEntity attempt = QuizAttemptEntity.builder()
                .id(100)
                .userId(1)
                .quiz(quiz)
                .totalQuestion(1)
                .correctQuestion(1)
                .score(100.0)
                .submittedAt(Instant.now())
                .build();

        QuizAttemptAnswerEntity answer = QuizAttemptAnswerEntity.builder()
                .id(200)
                .quizAttempt(attempt)
                .quizQuestion(question)
                .selectedOption(option)
                .build();

        when(quizRepository.findByLessonId(1)).thenReturn(Optional.of(quiz));
        when(quizQuestionRepository.findByQuizIdWithOptions(1)).thenReturn(List.of(question));
        when(quizAttemptRepository.findTopByUserIdAndQuizIdOrderBySubmittedAtDesc(1, 1)).thenReturn(Optional.of(attempt));
        when(quizAttemptAnswerRepository.findByAttemptIdWithOptions(100)).thenReturn(List.of(answer));

        QuizDetailResponse response = quizService.getQuizDetailByLessonId(1, 1);

        assertNotNull(response);
        assertTrue(response.getSubmitted());
        assertEquals(100.0, response.getScore());
        assertEquals(1, response.getQuestions().size());
        assertEquals(10, response.getQuestions().get(0).getSelectedOptionId());
        assertEquals(10, response.getQuestions().get(0).getCorrectOptionId());
        assertTrue(response.getQuestions().get(0).getIsCorrect());
    }

    @Test
    void submitQuiz_QuizNotFound_ThrowsException() {
        QuizSubmitRequest request = new QuizSubmitRequest();
        when(quizRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(AppException.class, () -> quizService.submitQuiz(1, 1, request));
    }

    @Test
    void submitQuiz_QuestionsEmpty_ThrowsException() {
        QuizSubmitRequest request = new QuizSubmitRequest();
        QuizEntity quiz = QuizEntity.builder().id(1).build();

        when(quizRepository.findById(1)).thenReturn(Optional.of(quiz));
        when(quizQuestionRepository.findByQuizIdWithOptions(1)).thenReturn(Collections.emptyList());

        AppException ex = assertThrows(AppException.class, () -> quizService.submitQuiz(1, 1, request));
        assertEquals(ErrorCode.QUIZ_QUESTIONS_EMPTY, ex.getErrorCode());
    }

    @Test
    void submitQuiz_Success() {
        QuizEntity quiz = QuizEntity.builder().id(1).build();

        QuizOptionEntity opt1 = QuizOptionEntity.builder().id(10).isCorrect(true).build();
        QuizQuestionEntity q1 = QuizQuestionEntity.builder().id(5).options(List.of(opt1)).build();

        QuizOptionEntity opt2 = QuizOptionEntity.builder().id(20).isCorrect(false).build();
        QuizOptionEntity opt2Correct = QuizOptionEntity.builder().id(21).isCorrect(true).build();
        QuizQuestionEntity q2 = QuizQuestionEntity.builder().id(6).options(List.of(opt2, opt2Correct)).build();

        when(quizRepository.findById(1)).thenReturn(Optional.of(quiz));
        when(quizQuestionRepository.findByQuizIdWithOptions(1)).thenReturn(List.of(q1, q2));
        
        when(quizAttemptRepository.save(any(QuizAttemptEntity.class))).thenAnswer(i -> {
            QuizAttemptEntity a = i.getArgument(0);
            a.setId(100);
            return a;
        });

        when(quizMapper.toQuizOptionResultResponse(any(QuizOptionEntity.class))).thenAnswer(i -> {
            QuizOptionEntity option = i.getArgument(0);
            return QuizOptionResultResponse.builder()
                    .optionId(option.getId())
                    .content(option.getContent())
                    .build();
        });

        QuizSubmitRequest request = new QuizSubmitRequest();
        QuizSubmitRequest.AnswerItem ans1 = new QuizSubmitRequest.AnswerItem(5, 10); // Correct
        QuizSubmitRequest.AnswerItem ans2 = new QuizSubmitRequest.AnswerItem(6, 20); // Incorrect
        request.setAnswers(List.of(ans1, ans2));

        QuizSubmitResultResponse response = quizService.submitQuiz(1, 1, request);

        assertNotNull(response);
        assertEquals(100, response.getAttemptId());
        assertEquals(2, response.getTotalQuestion());
        assertEquals(1, response.getCorrectQuestion());
        assertEquals(50.0, response.getScore());
        verify(quizAttemptAnswerRepository, times(1)).saveAll(anyList());
    }
}
