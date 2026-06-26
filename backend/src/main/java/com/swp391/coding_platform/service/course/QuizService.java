package com.swp391.coding_platform.service.course;

import com.swp391.coding_platform.dto.request.QuizSubmitRequest;
import com.swp391.coding_platform.dto.response.QuizDetailResponse;
import com.swp391.coding_platform.dto.response.QuizSubmitResultResponse;

public interface QuizService {
    QuizDetailResponse getQuizDetailByLessonId(Integer lessonId, Integer userId);
    QuizSubmitResultResponse submitQuiz(Integer quizId, Integer userId, QuizSubmitRequest request);
}
