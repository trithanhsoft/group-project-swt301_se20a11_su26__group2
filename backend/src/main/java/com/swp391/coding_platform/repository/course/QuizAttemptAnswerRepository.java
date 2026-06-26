package com.swp391.coding_platform.repository.course;

import com.swp391.coding_platform.entity.course.QuizAttemptAnswerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuizAttemptAnswerRepository extends JpaRepository<QuizAttemptAnswerEntity, Integer> {

    @Query("SELECT qaa FROM QuizAttemptAnswerEntity qaa " +
           "LEFT JOIN FETCH qaa.selectedOption " +
           "WHERE qaa.quizAttempt.id = :attemptId")
    List<QuizAttemptAnswerEntity> findByAttemptIdWithOptions(@Param("attemptId") Integer attemptId);
}
