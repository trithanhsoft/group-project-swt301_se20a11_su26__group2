package com.swp391.coding_platform.service.course;

import com.swp391.coding_platform.dto.request.QuizSubmitRequest;
import com.swp391.coding_platform.dto.response.*;
import com.swp391.coding_platform.entity.course.*;
import com.swp391.coding_platform.exception.AppException;
import com.swp391.coding_platform.exception.ErrorCode;
import com.swp391.coding_platform.mapper.QuizMapper;
import com.swp391.coding_platform.repository.course.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QuizServiceImpl implements QuizService {

    QuizRepository quizRepository;
    QuizQuestionRepository quizQuestionRepository;
    QuizAttemptRepository quizAttemptRepository;
    QuizAttemptAnswerRepository quizAttemptAnswerRepository;
    QuizMapper quizMapper;

    @Override
    @Transactional(readOnly = true)
    public QuizDetailResponse getQuizDetailByLessonId(Integer lessonId, Integer userId) {
        log.info("[QuizService] getQuizDetailByLessonId - lessonId: {}, userId: {}", lessonId, userId);
        
        QuizEntity quiz = quizRepository.findByLessonId(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.QUIZ_NOT_FOUND));

        if (quiz.getLesson() != null && (
                quiz.getLesson().getStatus() == com.swp391.coding_platform.entity.enums.LessonStatus.INACTIVE ||
                quiz.getLesson().getStatus() == com.swp391.coding_platform.entity.enums.LessonStatus.PENDING_UPDATE)) {
            throw new AppException(ErrorCode.QUIZ_NOT_FOUND);
        }

        List<QuizQuestionEntity> questions = quizQuestionRepository.findByQuizIdWithOptions(quiz.getId());
        
        Optional<QuizAttemptEntity> attemptOpt = quizAttemptRepository
                .findTopByUserIdAndQuizIdOrderBySubmittedAtDesc(userId, quiz.getId());

        if (attemptOpt.isEmpty()) {
            // 1. User has NOT submitted the quiz yet: hide correct answers
            List<QuizQuestionResponse> questionResponses = new ArrayList<>();
            for (QuizQuestionEntity q : questions) {
                List<QuizOptionResponse> optionResponses = new ArrayList<>();
                for (QuizOptionEntity o : q.getOptions()) {
                    optionResponses.add(QuizOptionResponse.builder()
                            .optionId(o.getId())
                            .content(o.getContent())
                            .orderIndex(o.getOrderIndex())
                            .isCorrect(null) // Hide isCorrect for security
                            .build());
                }
                questionResponses.add(QuizQuestionResponse.builder()
                        .questionId(q.getId())
                        .content(q.getContent())
                        .orderIndex(q.getOrderIndex())
                        .options(optionResponses)
                        .selectedOptionId(null)
                        .correctOptionId(null)
                        .isCorrect(null)
                        .build());
            }

            return QuizDetailResponse.builder()
                    .quizId(quiz.getId())
                    .title(quiz.getTitle())
                    .submitted(false)
                    .score(null)
                    .totalQuestion(null)
                    .correctQuestion(null)
                    .submittedAt(null)
                    .questions(questionResponses)
                    .build();
        } else {
            // 2. User has ALREADY submitted the quiz: show correct/incorrect choices and score
            QuizAttemptEntity attempt = attemptOpt.get();
            List<QuizAttemptAnswerEntity> attemptAnswers = quizAttemptAnswerRepository
                    .findByAttemptIdWithOptions(attempt.getId());

            Map<Integer, Integer> studentAnswersMap = new HashMap<>();
            for (QuizAttemptAnswerEntity answer : attemptAnswers) {
                Integer selectedId = (answer.getSelectedOption() != null) ? answer.getSelectedOption().getId() : null;
                studentAnswersMap.put(answer.getQuizQuestion().getId(), selectedId);
            }

            List<QuizQuestionResponse> questionResponses = new ArrayList<>();
            for (QuizQuestionEntity q : questions) {
                Integer selectedOptionId = studentAnswersMap.get(q.getId());
                Integer correctOptionId = null;
                
                List<QuizOptionResponse> optionResponses = new ArrayList<>();
                for (QuizOptionEntity o : q.getOptions()) {
                    optionResponses.add(QuizOptionResponse.builder()
                            .optionId(o.getId())
                            .content(o.getContent())
                            .orderIndex(o.getOrderIndex())
                            .isCorrect(o.getIsCorrect()) // Safe to return since quiz is already submitted
                            .build());
                    if (Boolean.TRUE.equals(o.getIsCorrect())) {
                        correctOptionId = o.getId();
                    }
                }

                boolean isCorrect = selectedOptionId != null && selectedOptionId.equals(correctOptionId);

                questionResponses.add(QuizQuestionResponse.builder()
                        .questionId(q.getId())
                        .content(q.getContent())
                        .orderIndex(q.getOrderIndex())
                        .options(optionResponses)
                        .selectedOptionId(selectedOptionId)
                        .correctOptionId(correctOptionId)
                        .isCorrect(isCorrect)
                        .build());
            }

            return QuizDetailResponse.builder()
                    .quizId(quiz.getId())
                    .title(quiz.getTitle())
                    .submitted(true)
                    .score(attempt.getScore())
                    .totalQuestion(attempt.getTotalQuestion())
                    .correctQuestion(attempt.getCorrectQuestion())
                    .submittedAt(attempt.getSubmittedAt())
                    .questions(questionResponses)
                    .build();
        }
    }

    @Override
    @Transactional
    public QuizSubmitResultResponse submitQuiz(Integer quizId, Integer userId, QuizSubmitRequest request) {
        log.info("[QuizService] submitQuiz - quizId: {}, userId: {}", quizId, userId);

        QuizEntity quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new AppException(ErrorCode.QUIZ_NOT_FOUND));

        List<QuizQuestionEntity> questions = quizQuestionRepository.findByQuizIdWithOptions(quizId);
        if (questions.isEmpty()) {
            throw new AppException(ErrorCode.QUIZ_QUESTIONS_EMPTY);
        }

        Map<Integer, Integer> correctOptionMap = new HashMap<>(); // questionId -> correctOptionId
        Map<Integer, QuizOptionEntity> optionEntityMap = new HashMap<>(); // optionId -> OptionEntity

        for (QuizQuestionEntity question : questions) {
            for (QuizOptionEntity option : question.getOptions()) {
                optionEntityMap.put(option.getId(), option);
                if (Boolean.TRUE.equals(option.getIsCorrect())) {
                    correctOptionMap.put(question.getId(), option.getId());
                }
            }
        }

        int totalQuestions = questions.size();
        int correctQuestions = 0;
        Map<Integer, Integer> studentAnswersMap = new HashMap<>(); // questionId -> selectedOptionId

        if (request.getAnswers() != null) {
            for (QuizSubmitRequest.AnswerItem item : request.getAnswers()) {
                studentAnswersMap.put(item.getQuestionId(), item.getSelectedOptionId());

                Integer correctOptionId = correctOptionMap.get(item.getQuestionId());
                if (correctOptionId != null && correctOptionId.equals(item.getSelectedOptionId())) {
                    correctQuestions++;
                }
            }
        }

        double score = ((double) correctQuestions / totalQuestions) * 100.0;

        QuizAttemptEntity attempt = QuizAttemptEntity.builder()
                .userId(userId)
                .quiz(quiz)
                .totalQuestion(totalQuestions)
                .correctQuestion(correctQuestions)
                .score(score)
                .submittedAt(Instant.now())
                .build();
        attempt = quizAttemptRepository.save(attempt);

        List<QuizAttemptAnswerEntity> answersToSave = new ArrayList<>();
        for (QuizQuestionEntity question : questions) {
            Integer selectedId = studentAnswersMap.get(question.getId());
            QuizOptionEntity selectedOption = (selectedId != null) ? optionEntityMap.get(selectedId) : null;

            QuizAttemptAnswerEntity answer = QuizAttemptAnswerEntity.builder()
                    .quizAttempt(attempt)
                    .quizQuestion(question)
                    .selectedOption(selectedOption)
                    .build();
            answersToSave.add(answer);
        }
        quizAttemptAnswerRepository.saveAll(answersToSave);

        return mapToSubmitResultResponse(attempt, questions, studentAnswersMap);
    }

    private QuizSubmitResultResponse mapToSubmitResultResponse(
            QuizAttemptEntity attempt,
            List<QuizQuestionEntity> questions,
            Map<Integer, Integer> studentAnswersMap) {

        List<QuizQuestionResultResponse> results = new ArrayList<>();

        for (QuizQuestionEntity question : questions) {
            Integer selectedId = studentAnswersMap.get(question.getId());
            Integer correctOptionId = null;
            List<QuizOptionResultResponse> optionResults = new ArrayList<>();

            for (QuizOptionEntity option : question.getOptions()) {
                optionResults.add(quizMapper.toQuizOptionResultResponse(option));
                if (Boolean.TRUE.equals(option.getIsCorrect())) {
                    correctOptionId = option.getId();
                }
            }

            boolean isCorrect = selectedId != null && selectedId.equals(correctOptionId);

            QuizQuestionResultResponse qResult = QuizQuestionResultResponse.builder()
                    .questionId(question.getId())
                    .content(question.getContent())
                    .selectedOptionId(selectedId)
                    .correctOptionId(correctOptionId)
                    .isCorrect(isCorrect)
                    .options(optionResults)
                    .build();

            results.add(qResult);
        }

        return QuizSubmitResultResponse.builder()
                .attemptId(attempt.getId())
                .totalQuestion(attempt.getTotalQuestion())
                .correctQuestion(attempt.getCorrectQuestion())
                .score(attempt.getScore())
                .submittedAt(attempt.getSubmittedAt())
                .results(results)
                .build();
    }
}
